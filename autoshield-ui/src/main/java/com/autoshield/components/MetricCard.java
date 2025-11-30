package com.autoshield.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

/**
 * Reusable metric card component for displaying system metrics
 */
public class MetricCard extends VerticalLayout {
    
    private final H3 title;
    private final Span value;
    private final ProgressBar progressBar;
    private final Span details;
    
    public MetricCard(String titleText, String iconClass) {
        addClassName("metric-card");
        setSpacing(false);
        setPadding(true);
        setWidth("280px");
        
        Div header = new Div();
        header.addClassName("metric-header");
        
        title = new H3(titleText);
        title.addClassName("metric-title");
        
        value = new Span("0%");
        value.addClassName("metric-value");
        
        progressBar = new ProgressBar();
        progressBar.setWidth("100%");
        progressBar.setValue(0);
        
        details = new Span("");
        details.addClassName("metric-details");
        details.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-secondary-text-color)");
        
        add(title, value, progressBar, details);
        
        getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "var(--lumo-space-m)");
    }
    
    public void updateValue(Double percent) {
        if (percent == null) percent = 0.0;
        
        value.setText(String.format("%.1f%%", percent));
        progressBar.setValue(percent / 100.0);
        
        // Color coding
        if (percent > 90) {
            progressBar.getStyle().set("--vaadin-progress-value-color", "var(--lumo-error-color)");
            value.getStyle().set("color", "var(--lumo-error-color)");
        } else if (percent > 75) {
            progressBar.getStyle().set("--vaadin-progress-value-color", "var(--lumo-warning-color)");
            value.getStyle().set("color", "var(--lumo-warning-color)");
        } else {
            progressBar.getStyle().set("--vaadin-progress-value-color", "var(--lumo-success-color)");
            value.getStyle().set("color", "var(--lumo-success-color)");
        }
    }
    
    public void updateValue(Integer count) {
        value.setText(String.valueOf(count));
        progressBar.setVisible(false);
        
        if (count > 0) {
            value.getStyle().set("color", "var(--lumo-error-color)").set("font-size", "2.5rem");
        } else {
            value.getStyle().set("color", "var(--lumo-success-color)").set("font-size", "2.5rem");
        }
    }
    
    public void setDetails(String detailsText) {
        details.setText(detailsText);
    }
}
