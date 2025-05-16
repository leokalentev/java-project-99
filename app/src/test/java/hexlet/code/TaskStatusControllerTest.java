package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.JWTUtils;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;


@SpringBootTest
@AutoConfigureMockMvc
class TaskStatusControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TaskStatusRepository repository;
    @Autowired private ObjectMapper om;
    @Autowired private Faker faker;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JWTUtils jwtUtils;

    @BeforeEach
    void setup() {
        repository.deleteAll();
        userRepository.deleteAll();
    }

    private String createUserAndGetToken() {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(6, 12);
        User user = new User();
        user.setEmail(email);
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return jwtUtils.generateToken(email);
    }

    private String generateUniqueSlug() {
        return faker.lorem().characters(10, 15).toLowerCase();
    }

    private TaskStatus createAndSaveStatus() {
        String name = faker.lorem().sentence();
        String slug = generateUniqueSlug();
        TaskStatus status = new TaskStatus();
        status.setName(name);
        status.setSlug(slug);
        repository.save(status);
        return status;
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

        TaskStatus taskStatus = new TaskStatus();
        String name = faker.lorem().sentence();
        String slug = generateUniqueSlug();
        taskStatus.setName(name);
        taskStatus.setSlug(slug);
        repository.save(taskStatus);

        MvcResult mvcResult = mockMvc.perform(get("/api/task_statuses/" + taskStatus.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        assertThatJson(json).and(
                a -> a.node("name").isEqualTo(taskStatus.getName()),
                a -> a.node("slug").isEqualTo(taskStatus.getSlug()),
                a -> a.node("createdAt").isNotNull()

        );
    }

    @Test
    public void testPostTaskStatus() throws Exception {
        String token = createUserAndGetToken();

        String name = faker.lorem().sentence();
        String slug = generateUniqueSlug();
        TaskStatusCreateDTO taskStatusCreateDTO = new TaskStatusCreateDTO();
        taskStatusCreateDTO.setName(name);
        taskStatusCreateDTO.setSlug(slug);

        MvcResult mvcResult = mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(om.writeValueAsString(taskStatusCreateDTO)))
                .andExpect(status().isCreated())
                .andReturn();
        var json = mvcResult.getResponse().getContentAsString();
        assertThatJson(json).and(
                a -> a.node("name").isEqualTo(name),
                a -> a.node("slug").isEqualTo(slug)
        );
    }

    @Test
    public void testUpdateTaskStatus() throws Exception {
        String token = createUserAndGetToken();

        String name = faker.lorem().sentence();
        String slug = generateUniqueSlug();
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName(name);
        taskStatus.setSlug(slug);
        repository.save(taskStatus);

        String newName = faker.lorem().sentence();
        String newSlug = generateUniqueSlug();

        TaskStatusUpdateDTO updateDTO = new TaskStatusUpdateDTO();
        updateDTO.setName(JsonNullable.of(newName));
        updateDTO.setSlug(JsonNullable.of(newSlug));

        MvcResult mvcResult = mockMvc.perform(put("/api/task_statuses/" + taskStatus.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(om.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        assertThatJson(json).and(
                a -> a.node("name").isEqualTo(newName),
                a -> a.node("slug").isEqualTo(newSlug)
        );
    }

    @Test
    public void destroyTaskStatus() throws Exception {
        String token = createUserAndGetToken();

        String name = faker.lorem().sentence();
        String slug = generateUniqueSlug();
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName(name);
        taskStatus.setSlug(slug);
        repository.save(taskStatus);

        mockMvc.perform(delete("/api/task_statuses/" + taskStatus.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(repository.findById(taskStatus.getId())).isEmpty();
    }


}



