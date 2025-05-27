package hexlet.code.service;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + email));
        // Возвращаем стандартный UserDetails, в котором Spring хранит email, пароль и роли
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                // если у тебя нет ролей, можно дать пользователю пустой список авторитетов
                .authorities(List.of())
                .build();
    }
}
