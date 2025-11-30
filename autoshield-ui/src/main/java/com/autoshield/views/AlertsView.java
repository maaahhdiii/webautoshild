package com.autoshield.views;

import com.autoshield.dto.AlertDto;
import com.autoshield.services.BackendService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Alerts view displaying security events in a data grid
 */
@Route(value = "alerts", layout = MainLayout.class)
@PageTitle("Security Alerts - AutoShield")
@PermitAll
@Slf4j
public class AlertsView extends VerticalLayout {
    
    private final BackendService backendService;
    private final Grid<AlertDto> grid;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public AlertsView(BackendService backendService) {
        this.backendService = backendService;
        
        addClassName("alerts-view");
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        
        // Header
        H2 title = new H2("Security Alerts");
        title.getStyle().set("margin", "0");
        
        Button refreshButton = new Button("Refresh");
        refreshButton.addClickListener(e -> loadAlerts());
        
        HorizontalLayout header = new HorizontalLayout(title, createSpacer(), refreshButton);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        
        // Grid
        grid = new Grid<>(AlertDto.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("600px");
        
        configureGrid();
        
        add(header, grid);
        
        // Load data
        loadAlerts();
    }
    
    private void configureGrid() {
        grid.addColumn(alert -> alert.getTimestamp().format(DATE_FORMATTER))
                .setHeader("Timestamp")
                .setSortable(true)
                .setFlexGrow(2);
        
        grid.addColumn(new ComponentRenderer<>(alert -> {
                    Span badge = new Span(alert.getSeverity());
                    badge.getElement().getThemeList().add("badge");
                    badge.getStyle()
                            .set("padding", "4px 10px")
                            .set("border-radius", "var(--lumo-border-radius-s)")
                            .set("font-size", "0.875rem")
                            .set("font-weight", "bold");
                    
                    switch (alert.getSeverity().toUpperCase()) {
                        case "CRITICAL" -> badge.getStyle()
                                .set("background", "var(--lumo-error-color)")
                                .set("color", "white");
                        case "HIGH" -> badge.getStyle()
                                .set("background", "#ff6b35")
                                .set("color", "white");
                        case "MEDIUM" -> badge.getStyle()
                                .set("background", "var(--lumo-warning-color)")
                                .set("color", "var(--lumo-base-color)");
                        case "LOW" -> badge.getStyle()
                                .set("background", "var(--lumo-contrast-10pct)")
                                .set("color", "var(--lumo-body-text-color)");
                    }
                    return badge;
                }))
                .setHeader("Severity")
                .setSortable(true)
                .setFlexGrow(1);
        
        grid.addColumn(AlertDto::getType)
                .setHeader("Type")
                .setSortable(true)
                .setFlexGrow(2);
        
        grid.addColumn(AlertDto::getSourceIp)
                .setHeader("Source IP")
                .setSortable(true)
                .setFlexGrow(1);
        
        grid.addColumn(new ComponentRenderer<>(alert -> {
                    Span badge = new Span(alert.getStatus());
                    badge.getStyle()
                            .set("padding", "4px 10px")
                            .set("border-radius", "var(--lumo-border-radius-s)")
                            .set("font-size", "0.875rem");
                    
                    switch (alert.getStatus().toUpperCase()) {
                        case "ACTIVE" -> badge.getStyle()
                                .set("background", "var(--lumo-error-color-10pct)")
                                .set("color", "var(--lumo-error-color)");
                        case "RESOLVED" -> badge.getStyle()
                                .set("background", "var(--lumo-success-color-10pct)")
                                .set("color", "var(--lumo-success-color)");
                        case "IGNORED" -> badge.getStyle()
                                .set("background", "var(--lumo-contrast-10pct)")
                                .set("color", "var(--lumo-secondary-text-color)");
                    }
                    return badge;
                }))
                .setHeader("Status")
                .setSortable(true)
                .setFlexGrow(1);
        
        grid.addColumn(AlertDto::getActionTaken)
                .setHeader("Action Taken")
                .setFlexGrow(2);
        
        grid.addColumn(new ComponentRenderer<>(alert -> {
                    Button viewButton = new Button("View");
                    viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                    viewButton.addClickListener(e -> showAlertDetails(alert));
                    return viewButton;
                }))
                .setHeader("Actions")
                .setFlexGrow(1);
    }
    
    private void loadAlerts() {
        try {
            List<AlertDto> alerts = backendService.getRecentAlerts(168); // Last 7 days
            grid.setItems(alerts);
        } catch (Exception e) {
            log.error("Error loading alerts: {}", e.getMessage());
        }
    }
    
    private void showAlertDetails(AlertDto alert) {
        VerticalLayout layout = new VerticalLayout();
        
        layout.add(new Span("ID: " + alert.getId()));
        layout.add(new Span("Timestamp: " + alert.getTimestamp().format(DATE_FORMATTER)));
        layout.add(new Span("Severity: " + alert.getSeverity()));
        layout.add(new Span("Type: " + alert.getType()));
        layout.add(new Span("Source IP: " + (alert.getSourceIp() != null ? alert.getSourceIp() : "N/A")));
        layout.add(new Span("Status: " + alert.getStatus()));
        layout.add(new Span("Action: " + (alert.getActionTaken() != null ? alert.getActionTaken() : "None")));
        
        if (alert.getDetails() != null && !alert.getDetails().isBlank()) {
            layout.add(new Span("Details: " + alert.getDetails()));
        }
        
        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        dialog.setHeaderTitle("Alert Details");
        dialog.add(layout);
        
        Button closeButton = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(closeButton);
        
        dialog.open();
    }
    
    private com.vaadin.flow.component.Component createSpacer() {
        com.vaadin.flow.component.html.Div spacer = new com.vaadin.flow.component.html.Div();
        spacer.getStyle().set("flex-grow", "1");
        return spacer;
    }
}
