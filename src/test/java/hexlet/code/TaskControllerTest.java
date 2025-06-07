package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
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

import java.util.Set;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired private TaskRepository repository;
    @Autowired private ObjectMapper om;
    @Autowired private Faker faker;
    @Autowired private UserRepository userRepository;
    @Autowired private TaskStatusRepository taskStatusRepository;
    @Autowired private LabelRepository labelRepository;
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

    private User createAndSaveUser() {
        var user = new User();
        user.setEmail(faker.internet().emailAddress());
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setPassword(passwordEncoder.encode(faker.internet().password(3, 12)));
        return userRepository.save(user);
    }

    private TaskStatus createAndSaveStatus() {
        var status = new TaskStatus();
        var name = faker.lorem().sentence();
        var slug = faker.lorem().characters(10, 15).toLowerCase();
        status.setName(name);
        status.setSlug(slug);
        return taskStatusRepository.save(status);
    }

    private Label createAndSaveLabel() {
        var label = new Label();
        label.setName(faker.lorem().sentence());
        return labelRepository.save(label);
    }
    private Task createAndSaveTask() {
        var name = faker.lorem().sentence();
        var index = faker.number().positive();
        var description = faker.lorem().sentence();
        var taskStatus = createAndSaveStatus();
        var assignee = createAndSaveUser();
        var label = createAndSaveLabel();

        Task task = new Task();
        task.setName(name);
        task.setIndex(index);
        task.setDescription(description);
        task.setTaskStatus(taskStatus);
        task.setAssignee(assignee);
        task.setLabels(Set.of(label));
        repository.save(task);

        return task;
    }

    @Test
    public void index() throws Exception {
        String token = createUserAndGetToken();

        Task task = createAndSaveTask();

        var result = mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void filterByTitle() throws Exception {
        String token = createUserAndGetToken();

        Task task = createAndSaveTask();

        mockMvc.perform(get("/api/tasks")
                        .param("titleCont", "findme")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(task.getId()));
    }

    @Test
    public void filterByAssignee() throws Exception {
        String token = createUserAndGetToken();

        Task task = createAndSaveTask();

        mockMvc.perform(get("/api/tasks")
                        .param("assigneeId", task.getAssignee().getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].assignee_id").value(task.getAssignee().getId()));
    }

    @Test
    public void filterByStatus() throws Exception {
        String token = createUserAndGetToken();

        Task task = createAndSaveTask();

        mockMvc.perform(get("/api/tasks")
                        .param("status", task.getTaskStatus().getSlug())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value(task.getTaskStatus().getSlug()));
    }

    @Test
    public void filterByLabel() throws Exception {
        String token = createUserAndGetToken();

        Task task = createAndSaveTask();

        mockMvc.perform(get("/api/tasks")
                        .param("labelId", task.getLabels().iterator().next().getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].taskLabelIds[0]").value(task.getLabels().iterator().next().getId()));
    }
    @Test
    public void show() throws Exception {
        String token = createUserAndGetToken();

        Task task = createAndSaveTask();

        var result = mockMvc.perform(get("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertThatJson(json).and(
                a -> a.node("title").isEqualTo(task.getName()),
                a -> a.node("index").isEqualTo(task.getIndex()),
                a -> a.node("content").isEqualTo(task.getDescription()),
                a -> a.node("status").isEqualTo(task.getTaskStatus().getSlug()),
                a -> a.node("assignee_id").isEqualTo(task.getAssignee().getId()),
                a -> a.node("taskLabelIds").isArray().contains(task.getLabels().iterator().next().getId()),
                a -> a.node("createdAt").isNotNull()
        );
    }

    @Test
    public void createTask() throws Exception {
        String token = createUserAndGetToken();

        var name = faker.lorem().sentence();
        var index = faker.number().positive();
        var description = faker.lorem().sentence();
        var taskStatus = createAndSaveStatus();
        var assignee = createAndSaveUser();
        var label = createAndSaveLabel();

        TaskCreateDTO task = new TaskCreateDTO();
        task.setTitle(name);
        task.setIndex(index);
        task.setContent(description);
        task.setStatus(taskStatus.getSlug());
        task.setAssigneeId(assignee.getId());
        task.setLabelIds(Set.of(label.getId()));

        MvcResult mvcResult = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(om.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andReturn();
        var json = mvcResult.getResponse().getContentAsString();
        assertThatJson(json).and(
                a -> a.node("title").isEqualTo(task.getTitle()),
                a -> a.node("index").isEqualTo(task.getIndex()),
                a -> a.node("content").isEqualTo(task.getContent()),
                a -> a.node("status").isEqualTo(task.getStatus()),
                a -> a.node("assignee_id").isEqualTo(task.getAssigneeId()),
                a -> a.node("taskLabelIds").isArray(),
                a -> a.node("createdAt").isNotNull()
        );
    }

    @Test
    public void updateTask() throws Exception {
        String token = createUserAndGetToken();

        var oldTask = createAndSaveTask();

        var name = faker.lorem().sentence();
        var description = faker.lorem().sentence();
        var taskStatus = createAndSaveStatus();
        var assignee = createAndSaveUser();
        var label = createAndSaveLabel();

        TaskUpdateDTO task = new TaskUpdateDTO();
        task.setTitle(JsonNullable.of(name));
        task.setContent(JsonNullable.of(description));
        task.setStatus(JsonNullable.of(taskStatus.getSlug()));
        task.setAssigneeId(JsonNullable.of(assignee.getId()));
        task.setLabelIds(JsonNullable.of(Set.of(label.getId())));

        MvcResult mvcResult = mockMvc.perform(put("/api/tasks/" + oldTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(om.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        assertThatJson(json).and(
                a -> a.node("title").isEqualTo(task.getTitle()),
                a -> a.node("content").isEqualTo(task.getContent()),
                a -> a.node("status").isEqualTo(task.getStatus()),
                a -> a.node("assignee_id").isEqualTo(task.getAssigneeId()),
                a -> a.node("taskLabelIds").isArray(),
                a -> a.node("createdAt").isNotNull()
        );
    }

    @Test
    public void deleteTask() throws Exception {
        String token = createUserAndGetToken();

        var task = createAndSaveTask();

        mockMvc.perform(delete("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(repository.findById(task.getId())).isEmpty();
    }
}
