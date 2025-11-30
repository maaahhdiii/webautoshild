package com.autoshield.components;

import com.autoshield.dto.AlertDto;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Component displaying live alert feed
 */
public class AlertFeed extends VerticalLayout {
    
    private final VerticalLayout alertContainer;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public AlertFeed() {
        addClassName("alert-feed");
        setSpacing(false);
        setPadding(true);
        
        H4 header = new H4("Recent Alerts");
        header.getStyle().set("margin", "0 0 var(--lumo-space-m) 0");
        
        alertContainer = new VerticalLayout();
        alertContainer.setSpacing(true);
        alertContainer.setPadding(false);
        alertContainer.setMaxHeight("400px");
        alertContainer.getStyle().set("overflow-y", "auto");
        
        add(header, alertContainer);
        
        getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "var(--lumo-space-m)");
    }
    
    public void updateAlerts(List<AlertDto> alerts) {
        alertContainer.removeAll();
        
        if (alerts.isEmpty()) {
            Span noAlerts = new Span("No recent alerts");
            noAlerts.getStyle().set("color", "var(--lumo-secondary-text-color)");
            alertContainer.add(noAlerts);
            return;
        }
        
        alerts.stream()
            .limit(10)
            .forEach(alert -> {
                Div alertItem = createAlertItem(alert);
                alertContainer.add(alertItem);
            });
    }
    
    private Div createAlertItem(AlertDto alert) {
        Div item = new Div();
        item.addClassName("alert-item");
        
        Span time = new Span(alert.getTimestamp().format(TIME_FORMATTER));
        time.getStyle()
            .set("font-size", "0.875rem")
            .set("color", "var(--lumo-secondary-text-color)")
            .set("margin-right", "var(--lumo-space-s)");
        
        Span severity = new Span(alert.getSeverity());
        severity.addClassName("badge");
        severity.getStyle()
            .set("padding", "2px 8px")
            .set("border-radius", "var(--lumo-border-radius-s)")
            .set("font-size", "0.75rem")
            .set("font-weight", "bold")
            .set("margin-right", "var(--lumo-space-s)");
        
        switch (alert.getSeverity().toUpperCase()) {
            case "CRITICAL" -> severity.getStyle()
                    .set("background", "var(--lumo-error-color)")
                    .set("color", "white");
            case "HIGH" -> severity.getStyle()
                    .set("background", "#ff6b35")
                    .set("color", "white");
            case "MEDIUM" -> severity.getStyle()
                    .set("background", "var(--lumo-warning-color)")
                    .set("color", "var(--lumo-base-color)");
            case "LOW" -> severity.getStyle()
                    .set("background", "var(--lumo-contrast-10pct)")
                    .set("color", "var(--lumo-body-text-color)");
        }
        
        Span type = new Span(alert.getType());
        type.getStyle()
            .set("font-weight", "500")
            .set("margin-right", "var(--lumo-space-s)");
        
        Span sourceIp = new Span("from " + (alert.getSourceIp() != null ? alert.getSourceIp() : "unknown"));
        sourceIp.getStyle()
            .set("font-size", "0.875rem")
            .set("color", "var(--lumo-secondary-text-color)");
        
        item.add(time, severity, type, sourceIp);
        
        item.getStyle()
            .set("padding", "var(--lumo-space-s)")
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        
        return item;
    }
}
