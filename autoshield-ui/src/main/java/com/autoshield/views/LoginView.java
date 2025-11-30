package com.autoshield.views;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Login view for authentication
 */
@Route("login")
@PageTitle("Login - AutoShield")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    
    private final LoginForm loginForm;
    
    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        getStyle()
            .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");
        
        VerticalLayout loginCard = new VerticalLayout();
        loginCard.setWidth("400px");
        loginCard.setPadding(true);
        loginCard.setSpacing(true);
        loginCard.getStyle()
            .set("background", "white")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("box-shadow", "0 10px 40px rgba(0,0,0,0.2)");
        
        H1 title = new H1("🛡️ AutoShield");
        title.getStyle()
            .set("margin", "0")
            .set("text-align", "center")
            .set("color", "#667eea");
        
        Paragraph subtitle = new Paragraph("Security Monitoring Dashboard");
        subtitle.getStyle()
            .set("text-align", "center")
            .set("color", "var(--lumo-secondary-text-color)")
            .set("margin", "0 0 var(--lumo-space-l) 0");
        
        loginForm = new LoginForm();
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);
        
        Paragraph hint = new Paragraph("Default credentials: admin/admin123 or viewer/viewer123");
        hint.getStyle()
            .set("font-size", "0.875rem")
            .set("color", "var(--lumo-secondary-text-color)")
            .set("text-align", "center")
            .set("margin-top", "var(--lumo-space-m)");
        
        loginCard.add(title, subtitle, loginForm, hint);
        loginCard.setAlignItems(Alignment.STRETCH);
        
        add(loginCard);
    }
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
