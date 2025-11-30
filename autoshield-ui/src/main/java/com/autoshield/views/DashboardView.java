package com.autoshield.views;

import com.autoshield.components.AlertFeed;
import com.autoshield.components.MetricCard;
import com.autoshield.dto.SystemMetricDto;
import com.autoshield.services.BackendService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main dashboard view showing real-time metrics and alerts
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard - AutoShield")
@PermitAll
@Slf4j
public class DashboardView extends VerticalLayout {
    
    private final BackendService backendService;
    private final MetricCard cpuCard;
    private final MetricCard ramCard;
    private final MetricCard diskCard;
    private final MetricCard threatCard;
    private final AlertFeed alertFeed;
    private final ScheduledExecutorService scheduler;
    
    public DashboardView(BackendService backendService) {
        this.backendService = backendService;
        
        addClassName("dashboard-view");
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        
        // Header
        H2 title = new H2("Security Dashboard");
        title.getStyle().set("margin", "0");
        
        // Metric cards
        cpuCard = new MetricCard("CPU Usage", "cpu");
        ramCard = new MetricCard("RAM Usage", "ram");
        diskCard = new MetricCard("Disk Usage", "disk");
        threatCard = new MetricCard("Active Threats", "threat");
        
        HorizontalLayout metricsLayout = new HorizontalLayout(cpuCard, ramCard, diskCard, threatCard);
        metricsLayout.setWidthFull();
        metricsLayout.setSpacing(true);
        
        // Alert feed
        alertFeed = new AlertFeed();
        alertFeed.setWidthFull();
        
        // Quick actions
        HorizontalLayout actionsLayout = createQuickActions();
        
        add(title, metricsLayout, actionsLayout, alertFeed);
        
        // Start real-time updates
        scheduler = Executors.newSingleThreadScheduledExecutor();
        startAutoRefresh();
        
        // Initial load
        updateDashboard();
    }
    
    private HorizontalLayout createQuickActions() {
        Button scanButton = new Button("Scan Network");
        scanButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        scanButton.addClickListener(e -> openScanDialog());
        
        Button blockIpButton = new Button("Block IP");
        blockIpButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        blockIpButton.addClickListener(e -> openBlockIpDialog());
        
        Button healthButton = new Button("System Health");
        healthButton.addClickListener(e -> checkSystemHealth());
        
        HorizontalLayout layout = new HorizontalLayout(scanButton, blockIpButton, healthButton);
        layout.setSpacing(true);
        layout.getStyle().set("margin-top", "var(--lumo-space-m)");
        
        return layout;
    }
    
    private void updateDashboard() {
        try {
            // Fetch current metrics
            SystemMetricDto metrics = backendService.getCurrentMetrics();
            if (metrics != null) {
                UI ui = getUI().orElse(null);
                if (ui != null) {
                    ui.access(() -> {
                        cpuCard.updateValue(metrics.getCpuPercent());
                        ramCard.updateValue(metrics.getRamPercent());
                        diskCard.updateValue(metrics.getDiskPercent());
                        threatCard.updateValue(metrics.getActiveThreats() != null ? metrics.getActiveThreats() : 0);
                        
                        cpuCard.setDetails("Node: " + (metrics.getNodeId() != null ? metrics.getNodeId() : "impaandaa"));
                    });
                }
            }
            
            // Fetch recent alerts
            UI ui = getUI().orElse(null);
            if (ui != null) {
                ui.access(() -> {
                    alertFeed.updateAlerts(backendService.getRecentAlerts(24));
                });
            }
            
        } catch (Exception e) {
            log.error("Error updating dashboard: {}", e.getMessage());
        }
    }
    
    private void startAutoRefresh() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateDashboard();
            } catch (Exception e) {
                log.error("Error in auto-refresh: {}", e.getMessage());
            }
        }, 10, 10, TimeUnit.SECONDS);
    }
    
    private void openScanDialog() {
        VerticalLayout dialogLayout = new VerticalLayout();
        
        TextField ipField = new TextField("Target IP");
        ipField.setPlaceholder("192.168.1.1");
        ipField.setWidthFull();
        
        com.vaadin.flow.component.select.Select<String> scanTypeSelect = new com.vaadin.flow.component.select.Select<>();
        scanTypeSelect.setLabel("Scan Type");
        scanTypeSelect.setItems("quick", "full", "vulnerability");
        scanTypeSelect.setValue("quick");
        scanTypeSelect.setWidthFull();
        
        Button startButton = new Button("Start Scan", e -> {
            String ip = ipField.getValue();
            String scanType = scanTypeSelect.getValue();
            
            if (ip == null || ip.isBlank()) {
                Notification.show("Please enter an IP address", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            var result = backendService.triggerScan(ip, scanType);
            String status = (String) result.get("status");
            
            if ("ACCEPTED".equals(status) || result.containsKey("scanId")) {
                Notification.show("Scan initiated for " + ip, 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show("Failed to start scan", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        dialogLayout.add(ipField, scanTypeSelect, startButton);
        
        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        dialog.setHeaderTitle("Network Scan");
        dialog.add(dialogLayout);
        dialog.open();
    }
    
    private void openBlockIpDialog() {
        VerticalLayout dialogLayout = new VerticalLayout();
        
        TextField ipField = new TextField("IP Address");
        ipField.setPlaceholder("192.168.1.100");
        ipField.setWidthFull();
        
        TextField reasonField = new TextField("Reason");
        reasonField.setPlaceholder("Suspicious activity detected");
        reasonField.setWidthFull();
        
        Button blockButton = new Button("Block IP", e -> {
            String ip = ipField.getValue();
            String reason = reasonField.getValue();
            
            if (ip == null || ip.isBlank() || reason == null || reason.isBlank()) {
                Notification.show("Please fill all fields", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            boolean success = backendService.blockIp(ip, reason, 60, false);
            
            if (success) {
                Notification.show("IP " + ip + " has been blocked", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show("Failed to block IP", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        blockButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        
        dialogLayout.add(ipField, reasonField, blockButton);
        
        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        dialog.setHeaderTitle("Block IP Address");
        dialog.add(dialogLayout);
        dialog.open();
    }
    
    private void checkSystemHealth() {
        var health = backendService.getHealthStatus();
        String status = (String) health.get("status");
        
        if ("UP".equals(status)) {
            Notification.show("All systems operational", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            Notification.show("System health: " + status, 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }
}
