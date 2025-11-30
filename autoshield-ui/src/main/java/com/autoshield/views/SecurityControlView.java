package com.autoshield.views;

import com.autoshield.dto.FirewallRuleDto;
import com.autoshield.services.BackendService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;

/**
 * Security control view for manual operations (Admin only)
 */
@Route(value = "security", layout = MainLayout.class)
@PageTitle("Security Control - AutoShield")
@RolesAllowed("ADMIN")
@Slf4j
public class SecurityControlView extends VerticalLayout {
    
    private final BackendService backendService;
    private final Grid<FirewallRuleDto> firewallGrid;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public SecurityControlView(BackendService backendService) {
        this.backendService = backendService;
        
        addClassName("security-control-view");
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        
        // Header
        H2 title = new H2("Security Control");
        title.getStyle().set("margin", "0 0 var(--lumo-space-l) 0");
        
        // Manual scan section
        VerticalLayout scanSection = createScanSection();
        
        // Firewall rules section
        H3 firewallTitle = new H3("Firewall Rules");
        firewallTitle.getStyle().set("margin", "var(--lumo-space-l) 0 var(--lumo-space-m) 0");
        
        VerticalLayout addRuleSection = createAddRuleSection();
        
        firewallGrid = new Grid<>(FirewallRuleDto.class, false);
        firewallGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        firewallGrid.setHeight("400px");
        configureFirewallGrid();
        
        // Service health section
        VerticalLayout healthSection = createHealthSection();
        
        add(title, scanSection, firewallTitle, addRuleSection, firewallGrid, healthSection);
        
        // Load data
        loadFirewallRules();
    }
    
    private VerticalLayout createScanSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");
        
        H3 sectionTitle = new H3("Manual Security Scan");
        sectionTitle.getStyle().set("margin-top", "0");
        
        TextField ipField = new TextField("Target IP or Range");
        ipField.setPlaceholder("192.168.1.1 or 192.168.1.0/24");
        ipField.setWidth("300px");
        
        Select<String> scanType = new Select<>();
        scanType.setLabel("Scan Type");
        scanType.setItems("quick", "full", "vulnerability");
        scanType.setValue("quick");
        scanType.setWidth("200px");
        
        Button startScanButton = new Button("Start Scan");
        startScanButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        startScanButton.addClickListener(e -> {
            String ip = ipField.getValue();
            String type = scanType.getValue();
            
            if (ip == null || ip.isBlank()) {
                Notification.show("Please enter an IP address", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            var result = backendService.triggerScan(ip, type);
            if (result.containsKey("scanId") || "ACCEPTED".equals(result.get("status"))) {
                Notification.show("Scan started for " + ip, 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                ipField.clear();
            } else {
                Notification.show("Failed to start scan", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        HorizontalLayout scanControls = new HorizontalLayout(ipField, scanType, startScanButton);
        scanControls.setAlignItems(Alignment.END);
        
        section.add(sectionTitle, scanControls);
        return section;
    }
    
    private VerticalLayout createAddRuleSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");
        
        TextField ipField = new TextField("IP Address");
        ipField.setPlaceholder("192.168.1.100");
        ipField.setWidth("200px");
        
        TextField reasonField = new TextField("Reason");
        reasonField.setPlaceholder("Suspicious activity");
        reasonField.setWidth("300px");
        
        IntegerField durationField = new IntegerField("Duration (minutes)");
        durationField.setValue(60);
        durationField.setWidth("150px");
        
        Checkbox permanentCheckbox = new Checkbox("Permanent");
        permanentCheckbox.addValueChangeListener(e -> {
            durationField.setEnabled(!e.getValue());
        });
        
        Button addButton = new Button("Add Rule");
        addButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        addButton.addClickListener(e -> {
            String ip = ipField.getValue();
            String reason = reasonField.getValue();
            Integer duration = durationField.getValue();
            Boolean permanent = permanentCheckbox.getValue();
            
            if (ip == null || ip.isBlank() || reason == null || reason.isBlank()) {
                Notification.show("Please fill all required fields", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            boolean success = backendService.blockIp(ip, reason, duration, permanent);
            if (success) {
                Notification.show("IP blocked successfully", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                ipField.clear();
                reasonField.clear();
                loadFirewallRules();
            } else {
                Notification.show("Failed to block IP", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        HorizontalLayout controls = new HorizontalLayout(ipField, reasonField, durationField, permanentCheckbox, addButton);
        controls.setAlignItems(Alignment.END);
        
        section.add(controls);
        return section;
    }
    
    private void configureFirewallGrid() {
        firewallGrid.addColumn(FirewallRuleDto::getIpAddress)
                .setHeader("IP Address")
                .setSortable(true);
        
        firewallGrid.addColumn(FirewallRuleDto::getReason)
                .setHeader("Reason")
                .setSortable(true);
        
        firewallGrid.addColumn(rule -> rule.getCreatedAt().format(DATE_FORMATTER))
                .setHeader("Blocked At")
                .setSortable(true);
        
        firewallGrid.addColumn(rule -> rule.getExpiresAt() != null ? 
                        rule.getExpiresAt().format(DATE_FORMATTER) : "Permanent")
                .setHeader("Expires At")
                .setSortable(true);
        
        firewallGrid.addColumn(new ComponentRenderer<>(rule -> {
                    Button removeButton = new Button("Remove");
                    removeButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                    removeButton.addClickListener(e -> removeRule(rule));
                    return removeButton;
                }))
                .setHeader("Actions");
    }
    
    private void loadFirewallRules() {
        try {
            var rules = backendService.getFirewallRules();
            firewallGrid.setItems(rules);
        } catch (Exception e) {
            log.error("Error loading firewall rules: {}", e.getMessage());
        }
    }
    
    private void removeRule(FirewallRuleDto rule) {
        boolean success = backendService.unblockIp(rule.getIpAddress());
        if (success) {
            Notification.show("IP unblocked successfully", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadFirewallRules();
        } else {
            Notification.show("Failed to unblock IP", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private VerticalLayout createHealthSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-top", "var(--lumo-space-l)");
        
        H3 sectionTitle = new H3("Service Health Status");
        sectionTitle.getStyle().set("margin-top", "0");
        
        Button checkHealthButton = new Button("Check Health");
        checkHealthButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        checkHealthButton.addClickListener(e -> {
            var health = backendService.getHealthStatus();
            String status = (String) health.get("status");
            Notification notification = Notification.show("System Status: " + status, 3000, Notification.Position.BOTTOM_START);
            if ("UP".equals(status)) {
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });
        
        section.add(sectionTitle, checkHealthButton);
        return section;
    }
}
