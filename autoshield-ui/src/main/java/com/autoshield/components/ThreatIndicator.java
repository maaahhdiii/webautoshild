package com.autoshield.components;

import com.vaadin.flow.component.html.Span;

/**
 * Component displaying threat level indicator
 */
public class ThreatIndicator extends Span {
    
    public ThreatIndicator() {
        addClassName("threat-indicator");
        setText("LOW");
        updateThreatLevel(0);
    }
    
    public void updateThreatLevel(int activeThreats) {
        getStyle()
            .set("padding", "4px 12px")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("font-weight", "bold")
            .set("font-size", "0.875rem");
        
        if (activeThreats == 0) {
            setText("LOW");
            getStyle()
                .set("background", "var(--lumo-success-color-10pct)")
                .set("color", "var(--lumo-success-color)");
        } else if (activeThreats < 5) {
            setText("MEDIUM");
            getStyle()
                .set("background", "var(--lumo-warning-color-10pct)")
                .set("color", "var(--lumo-warning-color)");
        } else if (activeThreats < 10) {
            setText("HIGH");
            getStyle()
                .set("background", "#ff6b3510")
                .set("color", "#ff6b35");
        } else {
            setText("CRITICAL");
            getStyle()
                .set("background", "var(--lumo-error-color-10pct)")
                .set("color", "var(--lumo-error-color)");
        }
    }
}
