package org.example;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
@DisplayName("Application Test")
public class AppTest {
    App app;
    @BeforeEach
    void setup() {
        app = new App();
    }
    @Test
    @DisplayName("Test Application name")
    void testAppName() {
        assertThat(app).isNotNull()
                       .extracting(App::getName, App::getVersion)
                       .isNotNull().containsExactly("app name", 1.0);
    }

}