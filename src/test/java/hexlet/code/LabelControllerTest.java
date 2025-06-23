package hexlet.code;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
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

import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@SpringBootTest
@AutoConfigureMockMvc
public class LabelControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired private LabelRepository repository;
    @Autowired private ObjectMapper om;
    @Autowired private Faker faker;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JWTUtils jwtUtils;

    @Autowired
    private LabelMapper mapper;

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

    private Label createAndSaveLabel() {
        String name = faker.lorem().sentence();
        Label label = new Label();
        label.setName(name);
        repository.save(label);

        return label;
    }

    @Test
    public void index() throws Exception {
        String token = createUserAndGetToken();

        Label label1 = createAndSaveLabel();
        Label label2 = createAndSaveLabel();
        List<Label> labelsFromDb = List.of(label1, label2);

        List<LabelDTO> expectedDtos = labelsFromDb.stream()
                .map(mapper::map)
                .toList();

        var result = mockMvc.perform(get("/api/labels")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();

        List<LabelDTO> actualDtos = om.readValue(
                json,
                new TypeReference<List<LabelDTO>>() { }
        );

        assertThat(actualDtos)
                .usingRecursiveComparison()
                .isEqualTo(expectedDtos);
    }


    @Test
    public void showLabel() throws Exception {
        String token = createUserAndGetToken();

        String name = faker.lorem().sentence();
        Label label = new Label();
        label.setName(name);
        repository.save(label);

        MvcResult mvcResult = mockMvc.perform(get("/api/labels/" + label.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        assertThatJson(json).and(
                a -> a.node("name").isEqualTo(label.getName()),
                a -> a.node("createdAt").isNotNull()
        );
    }

    @Test
    public void createLabel() throws Exception {
        String token = createUserAndGetToken();

        String name = faker.lorem().sentence();
        LabelCreateDTO label = new LabelCreateDTO();
        label.setName(name);

        MvcResult mvcResult = mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(om.writeValueAsString(label)))
                .andExpect(status().isCreated())
                .andReturn();
        var json = mvcResult.getResponse().getContentAsString();
        assertThatJson(json).and(
                a -> a.node("name").isEqualTo(name)
        );
    }

    @Test
    public void updateLabel() throws Exception {
        String token = createUserAndGetToken();

        String name = faker.lorem().sentence();
        Label label = new Label();
        label.setName(name);
        repository.save(label);

        String newName = faker.lorem().sentence();
        LabelUpdateDTO newLabel = new LabelUpdateDTO();
        newLabel.setName(JsonNullable.of(newName));

        MvcResult mvcResult = mockMvc.perform(put("/api/labels/" + label.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(om.writeValueAsString(newLabel)))
                .andExpect(status().isOk())
                .andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        assertThatJson(json).and(
                a -> a.node("name").isEqualTo(newName)
        );
    }

    @Test
    public void deleteLabel() throws Exception {
        String token = createUserAndGetToken();

        String name = faker.lorem().sentence();
        Label label = new Label();
        label.setName(name);
        repository.save(label);

        mockMvc.perform(delete("/api/labels/" + label.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(repository.findById(label.getId())).isEmpty();
    }
}
