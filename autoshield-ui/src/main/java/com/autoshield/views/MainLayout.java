package com.autoshield.views;

import com.autoshield.components.ThreatIndicator;
import com.autoshield.security.SecurityService;
import com.autoshield.services.BackendService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * Main application layout with header and navigation
 */
@Slf4j
public class MainLayout extends AppLayout {
    
    private final SecurityService securityService;
    private final BackendService backendService;
    private final ThreatIndicator connectionStatus;
    
    public MainLayout(SecurityService securityService, BackendService backendService) {
        this.securityService = securityService;
        this.backendService = backendService;
        this.connectionStatus = new ThreatIndicator();
        
        createHeader();
        createDrawer();
    }
    
    private void createHeader() {
        H1 logo = new H1("🛡️ AutoShield");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE
        );
        logo.getStyle().set("color", "#667eea");
        
        // Connection status
        connectionStatus.setText("Connecting...");
        connectionStatus.getStyle()
            .set("background", "var(--lumo-contrast-10pct)")
            .set("color", "var(--lumo-secondary-text-color)");
        updateConnectionStatus();
        
        // User info
        String username = securityService.getCurrentUsername();
        String role = securityService.isAdmin() ? "ADMIN" : "VIEWER";
        
        Span userInfo = new Span(username);
        userInfo.getStyle()
            .set("margin-right", "var(--lumo-space-s)")
            .set("color", "var(--lumo-secondary-text-color)");
        
        Span roleBadge = new Span(role);
        roleBadge.getStyle()
            .set("padding", "2px 8px")
            .set("border-radius", "var(--lumo-border-radius-s)")
            .set("background", securityService.isAdmin() ? 
                "var(--lumo-primary-color-10pct)" : "var(--lumo-contrast-10pct)")
            .set("color", securityService.isAdmin() ? 
                "var(--lumo-primary-color)" : "var(--lumo-body-text-color)")
            .set("font-size", "0.75rem")
            .set("font-weight", "bold")
            .set("margin-right", "var(--lumo-space-m)");
        
        Button logout = new Button("Logout", new Icon(VaadinIcon.SIGN_OUT));
        logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logout.addClickListener(e -> securityService.logout());
        
        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                logo,
                createSpacer(),
                connectionStatus,
                createSpacer(),
                userInfo,
                roleBadge,
                logout
        );
        
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassName("header");
        header.getStyle()
            .set("padding", "var(--lumo-space-m) var(--lumo-space-l)")
            .set("background", "white")
            .set("box-shadow", "0 1px 4px rgba(0,0,0,0.1)");
        
        addToNavbar(header);
    }
    
    private void createDrawer() {
        SideNav nav = new SideNav();
        
        nav.addItem(new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Alerts", AlertsView.class, VaadinIcon.WARNING.create()));
        
        if (securityService.isAdmin()) {
            nav.addItem(new SideNavItem("Security Control", SecurityControlView.class, VaadinIcon.SHIELD.create()));
        }
        
        nav.addItem(new SideNavItem("Settings", SettingsView.class, VaadinIcon.COG.create()));
        
        addToDrawer(nav);
    }
    
    private Component createSpacer() {
        Div spacer = new Div();
        spacer.getStyle().set("flex-grow", "1");
        return spacer;
    }
    
    private void updateConnectionStatus() {
        try {
            boolean connected = backendService.testConnection();
            if (connected) {
                connectionStatus.setText("Connected");
                connectionStatus.getStyle()
                    .set("background", "var(--lumo-success-color-10pct)")
                    .set("color", "var(--lumo-success-color)");
            } else {
                connectionStatus.setText("Disconnected");
                connectionStatus.getStyle()
                    .set("background", "var(--lumo-error-color-10pct)")
                    .set("color", "var(--lumo-error-color)");
            }
        } catch (Exception e) {
            log.error("Error checking connection: {}", e.getMessage());
            connectionStatus.setText("Error");
            connectionStatus.getStyle()
                .set("background", "var(--lumo-error-color-10pct)")
                .set("color", "var(--lumo-error-color)");
        }
    }
}
