package com.autoshield;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for AutoShield UI
 */
@SpringBootApplication
@Theme(value = "autoshield")
@Push
public class AutoShieldUiApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(AutoShieldUiApplication.class, args);
    }
}
