package hexlet.code.component;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DotenvLoader {

    @PostConstruct
    public void loadEnv() {
        Dotenv dotenv = Dotenv.load();

        System.setProperty("SENTRY_DSN", dotenv.get("SENTRY_DSN"));
        System.setProperty("SENTRY_ENVIRONMENT", dotenv.get("SENTRY_ENVIRONMENT"));
        System.out.println(".env переменные загружены");
    }
}

