package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserDTO> getAll() {
        var users = userRepository.findAll();
        return users.stream().map(userMapper::map).toList();
    }

    public UserDTO showUser(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        var userDTO = userMapper.map(user);
        return userDTO;
    }

    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        var user = userMapper.map(userCreateDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        var userDTO = userMapper.map(user);
        return userDTO;
    }

    public UserDTO updateUser(Long id, UserUpdateDTO dto) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (dto.getEmail() != null) {
            if (!isValidEmail(dto.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный email");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }

        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }

        if (dto.getPassword() != null) {
            if (dto.getPassword().length() < 3) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароль должен содержать минимум 3 символа");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
        return userMapper.map(user);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,}$");
    }
    public void deleteUser(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.deleteById(id);
    }
}
