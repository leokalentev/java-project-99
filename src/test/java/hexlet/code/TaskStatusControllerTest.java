package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.JWTUtils;
import jakarta.transaction.Transactional;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@ActiveProfiles("test")
class TaskStatusControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TaskStatusRepository repository;
    @Autowired private ObjectMapper om;
    @Autowired private Faker faker;
    @Autowired private UserRepository userRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JWTUtils jwtUtils;

    @BeforeEach
    void setup() {
        taskRepository.deleteAll();
        repository.deleteAll();
        userRepository.deleteAll();
    }

    private String createUserAndGetToken() {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        String password = "password";

        User user = new User();
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);
        return jwtUtils.generateToken(email);
    }

    private String generateUniqueSlug() {
        return "slug-" + UUID.randomUUID();
    }

    private TaskStatus createAndSaveStatus() {
        String name = "status-" + UUID.randomUUID();
        String slug = generateUniqueSlug();

        TaskStatus status = new TaskStatus();
        status.setName(name);
        status.setSlug(slug);

        return repository.save(status);
    }

    @Test
    public void testIndex() throws Exception {
        String token = createUserAndGetToken();
        createAndSaveStatus();

        var result = mockMvc.perform(get("/api/task_statuses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testShowTaskStatus() throws Exception {
        String token = createUserAndGetToken();
        TaskStatus taskStatus = createAndSaveStatus();

        MvcResult result = mockMvc.perform(get("/api/task_statuses/" + taskStatus.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        var json = result.getResponse().getContentAsString();
        assertThatJson(json).and(
                a -> a.node("name").isEqualTo(taskStatus.getName()),
                a -> a.node("slug").isEqualTo(taskStatus.getSlug()),
                a -> a.node("createdAt").isNotNull()
        );
    }

    @Test
    public void testPostTaskStatus() throws Exception {
        String token = createUserAndGetToken();

        String name = "status-" + UUID.randomUUID();
        String slug = generateUniqueSlug();

        TaskStatusCreateDTO dto = new TaskStatusCreateDTO();
        dto.setName(name);
        dto.setSlug(slug);

        MvcResult result = mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertThatJson(json).and(
                a -> a.node("name").isEqualTo(name),
                a -> a.node("slug").isEqualTo(slug)
        );
    }

    @Test
    public void testUpdateTaskStatus() throws Exception {
        String token = createUserAndGetToken();
        TaskStatus taskStatus = createAndSaveStatus();

        String newName = "updated-status-" + UUID.randomUUID();
        String newSlug = generateUniqueSlug();

        TaskStatusUpdateDTO updateDTO = new TaskStatusUpdateDTO();
        updateDTO.setName(JsonNullable.of(newName));
        updateDTO.setSlug(JsonNullable.of(newSlug));

        MvcResult result = mockMvc.perform(put("/api/task_statuses/" + taskStatus.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(om.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertThatJson(json).and(
                a -> a.node("name").isEqualTo(newName),
                a -> a.node("slug").isEqualTo(newSlug)
        );
    }

    @Test
    public void destroyTaskStatus() throws Exception {
        String token = createUserAndGetToken();
        TaskStatus taskStatus = createAndSaveStatus();

        mockMvc.perform(delete("/api/task_statuses/" + taskStatus.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(repository.findById(taskStatus.getId())).isEmpty();
    }
}



