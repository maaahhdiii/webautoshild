package com.autoshield.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

/**
 * Settings view for configuration (placeholder)
 */
@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Settings - AutoShield")
@PermitAll
public class SettingsView extends VerticalLayout {
    
    public SettingsView() {
        addClassName("settings-view");
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        
        H2 title = new H2("Settings");
        Paragraph description = new Paragraph("System configuration and preferences will be available here.");
        
        add(title, description);
    }
}
