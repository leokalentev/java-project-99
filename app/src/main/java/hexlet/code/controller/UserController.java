package hexlet.code.controller;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.UserUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserUtils userUtils;

    @GetMapping
    public List<UserDTO> index() {
        var users = userRepository.findAll();
        return users.stream().map(userMapper::map).toList();
    }

    @GetMapping(path = "/{id}")
    public UserDTO show(@PathVariable Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        var userDTO = userMapper.map(user);
        return userDTO;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@RequestBody @Valid UserCreateDTO userCreateDTO) {
        var user = userMapper.map(userCreateDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        var userDTO = userMapper.map(user);
        return userDTO;
    }

    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO userUpdateDTO) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!userUtils.getCurrentUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        if (userUpdateDTO.getEmail() != null) {
            if (!isValidEmail(userUpdateDTO.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный email");
            }
            user.setEmail(userUpdateDTO.getEmail());
        }

        if (userUpdateDTO.getFirstName() != null) {
            user.setFirstName(userUpdateDTO.getFirstName());
        }

        if (userUpdateDTO.getLastName() != null) {
            user.setLastName(userUpdateDTO.getLastName());
        }

        if (userUpdateDTO.getPassword() != null) {
            if (userUpdateDTO.getPassword().length() < 3) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароль должен содержать минимум 3 символа");
            }
            user.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
        }

        userRepository.save(user);
        return userMapper.map(user);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,}$");
    }

    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!userUtils.getCurrentUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        userRepository.deleteById(id);
    }
}
