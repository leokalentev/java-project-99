package hexlet.code;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@SpringBootApplication
@RestController
@EnableJpaAuditing
public class AppApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

    @GetMapping(path = "/welcome")
    public String index() {
        return "Welcome to Spring!";
    }

    @Bean
    public Faker getFaker() {
        return new Faker();
    }

    @Bean
    public CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "hexlet@example.com";
            Optional<User> existingUser = userRepository.findByEmail(adminEmail);

            if (existingUser.isEmpty()) {
                var user = new User();
                user.setEmail(adminEmail);
                user.setPassword(passwordEncoder.encode("qwerty"));
                user.setFirstName("Admin");
                user.setLastName("Hexlet");
                userRepository.save(user);
                System.out.println("Admin user created with email: " + adminEmail);
            } else {
                System.out.println("Admin user already exists with email: " + adminEmail);
            }
        };
    }
}
