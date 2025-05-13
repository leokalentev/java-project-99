package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testWelcomePage() throws Exception {
        var result = mockMvc.perform(get("/welcome"))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThat(body).contains("Welcome to Spring!");
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testShow() throws Exception {
        var firstName = faker.name().firstName();
        var lastName = faker.name().lastName();
        var email = faker.internet().emailAddress();
        var password = faker.internet().password(3, 12);

        var user = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getFirstName), () -> firstName)
                .supply(Select.field(User::getLastName), () -> lastName)
                .supply(Select.field(User::getEmail), () -> email)
                .supply(Select.field(User::getPassword), () -> password)
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdateAt))
                .create();
        userRepository.save(user);

        MvcResult mvcResult = mockMvc.perform(get("/api/users/" + user.getId()).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        assertThatJson(json).and(
                a -> a.node("firstName").isEqualTo(firstName),
                a -> a.node("lastName").isEqualTo(lastName),
                a -> a.node("email").isEqualTo(email),
                a -> a.node("createdAt").isNotNull()
        );
    }

    @Test
    public void testCreate() throws Exception {
        var firstName = faker.name().firstName();
        var lastName = faker.name().lastName();
        var email = faker.internet().emailAddress();
        var password = faker.internet().password(3, 12);

        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setFirstName(firstName);
        userCreateDTO.setLastName(lastName);
        userCreateDTO.setEmail(email);
        userCreateDTO.setPassword(password);

        MvcResult mvcResult = mockMvc.perform(post("/api/users")
                .contentType(APPLICATION_JSON)
                .content(om.writeValueAsString(userCreateDTO)))
                .andExpect(status().isCreated())
                .andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        assertThatJson(json).and(
                a -> a.node("firstName").isEqualTo(firstName),
                a -> a.node("lastName").isEqualTo(lastName),
                a -> a.node("email").isEqualTo(email),
                a -> a.node("createdAt").isNotNull()
        );
    }

    @Test
    public void testUpdateUser() throws Exception {
        String oldEmail = faker.internet().emailAddress();
        String oldFirstName = faker.name().firstName();
        String oldLastName = faker.name().lastName();
        String oldPassword = faker.internet().password(3, 12);

        var user = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> oldEmail)
                .supply(Select.field(User::getFirstName), () -> oldFirstName)
                .supply(Select.field(User::getLastName), () -> oldLastName)
                .supply(Select.field(User::getPassword), () -> passwordEncoder.encode(oldPassword))
                .create();
        userRepository.save(user);

        String newFirstName = faker.name().firstName();
        Map<String, Object> updateData = Map.of("firstName", newFirstName);

        MvcResult result = mockMvc.perform(
                        put("/api/users/" + user.getId())
                                .contentType(APPLICATION_JSON)
                                .content(om.writeValueAsString(updateData))
                )
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThatJson(responseBody).and(
                a -> a.node("firstName").isEqualTo(newFirstName),
                a -> a.node("lastName").isEqualTo(oldLastName),
                a -> a.node("email").isEqualTo(oldEmail)
        );
    }

    @Test
    public void testDeleteUser() throws Exception {
        var user = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getPassword), () -> passwordEncoder.encode(faker.internet().password(3, 12)))
                .create();
        userRepository.save(user);

        mockMvc.perform(delete("/api/users/" + user.getId()))
                .andExpect(status().isOk());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

}
