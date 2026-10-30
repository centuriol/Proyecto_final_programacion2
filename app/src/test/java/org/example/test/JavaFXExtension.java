package org.example.test;

import javafx.application.Platform;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Extensión JUnit 5 reutilizable para inicializar el Toolkit de JavaFX una sola vez (DRY / SRP).
 */
public class JavaFXExtension implements BeforeAllCallback {

    private static volatile boolean inicializado = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!inicializado) {
            synchronized (JavaFXExtension.class) {
                if (!inicializado) {
                    try {
                        Platform.startup(() -> {});
                    } catch (IllegalStateException ignored) {}
                    inicializado = true;
                }
            }
        }
    }
}
