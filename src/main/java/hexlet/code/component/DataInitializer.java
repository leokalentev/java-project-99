package hexlet.code.component;

import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Configuration
@Profile("!test")
public class DataInitializer {

    private static final String ADMIN_EMAIL = "hexlet@example.com";

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      TaskStatusRepository statusRepository,
                                      LabelRepository labelRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            initAdmin(userRepository, passwordEncoder);
            initTaskStatuses(statusRepository);
            initLabels(labelRepository);
        };
    }

    private void initAdmin(UserRepository userRepository, PasswordEncoder encoder) {
        Optional<User> existingUser = userRepository.findByEmail(ADMIN_EMAIL);

        if (existingUser.isEmpty()) {
            var user = new User();
            user.setEmail(ADMIN_EMAIL);
            user.setPassword(encoder.encode("qwerty"));
            user.setFirstName("Admin");
            user.setLastName("Hexlet");
            userRepository.save(user);
            System.out.println("Admin user created with email: " + ADMIN_EMAIL);
        } else {
            System.out.println("Admin user already exists with email: " + ADMIN_EMAIL);
        }
    }

    private void initTaskStatuses(TaskStatusRepository statusRepository) {
        List<String> slugs = List.of("draft", "to_review", "to_be_fixed", "to_publish", "published");

        for (String slug : slugs) {
            boolean exists = statusRepository.existsBySlug(slug);
            if (!exists) {
                var status = new TaskStatus();
                status.setSlug(slug);
                status.setName(slug);
                statusRepository.save(status);
                System.out.println("Created status: " + slug);
            }
        }
    }

    private void initLabels(LabelRepository repository) {
        List<String> defaultLabels = List.of("feature", "bug");

        for (String name : defaultLabels) {
            boolean exists = repository.existsByName(name);
            if (!exists) {
                var label = new Label();
                label.setName(name);
                repository.save(label);
                System.out.println("Created label: " + name);
            }
        }
    }
}


