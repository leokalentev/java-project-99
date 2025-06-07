package hexlet.code.component;

import hexlet.code.utils.UserUtils;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    private final UserUtils userUtils;

    public UserSecurity(UserUtils userUtils) {
        this.userUtils = userUtils;
    }

    public boolean isOwner(Long userId) {
        var currentUser = userUtils.getCurrentUser();
        return currentUser != null && currentUser.getId().equals(userId);
    }
}

