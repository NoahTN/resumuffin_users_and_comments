package in.resumuff.core.users.logic;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import in.resumuff.core.users.db.RoleRepository;
import in.resumuff.core.users.db.UserRepository;
import in.resumuff.core.users.entities.User;

@Service
public class UserService{
    private static final short ROLE_ADMIN = 1;
    private static final short ROLE_USER = 2;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PasswordEncoder pwEncoder;

    public boolean registerUser(String email, String username, String password) {
        if(!userRepo.existsByEmail(email) && !userRepo.existsByUsername(username)) {
            User user = new User(email, username, pwEncoder.encode(password));
            user.setRole(roleRepo.findById(ROLE_USER).get());
            userRepo.save(user);
            return true;
        }
        return false;
    }

    // logging in
    public User authenticate(HttpSession session, String userOrEmail, String password) {
        User user;
        user = userRepo.findByEmail(userOrEmail).orElse(null);
        if(user == null) {
            user = userRepo.findByUsername(userOrEmail).orElse(null);
            if(user == null)
                return null;
        }

        if(pwEncoder.matches(password, user.getPassword())) {
            user.setAccessToken(StringGenerator.generate(32));
            session.setAttribute("TOKEN", user.getAccessToken());
            session.setAttribute("USER_ID", user.getId());
            userRepo.save(user);
            return user;
        }
        return null;
    }

    // should hide some info
    public User getUserDetails(String username) {
        return userRepo.findByUsername(username).orElse(null);
    }

    public User getUserDetails(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    public User getUserDetailsFull(HttpSession session, String username) {
        if(session.getAttribute("TOKEN") != null) {
            User user = userRepo.findByUsername(username).orElse(null);
            // should throw some appropriate errors
            if(user != null && user.getAccessToken().equals(session.getAttribute("TOKEN")))
                return user;
        }
        return null;
    }

    public List<User> getAllUsers() {
        return userRepo.findAllByOrderByIdAsc();
    }

    public Page<User> getUsers(int pageNum, int pageLen) {
        Pageable pageable = PageRequest.of(pageNum, pageLen);
        Page<User> users = userRepo.findAll(pageable);
        return users;
    }

    public boolean hasDeleteAbility(HttpSession session) {
        long userId = (long)session.getAttribute("USER_ID");
        if(session.getAttribute("TOKEN") != null) {
            User user = userRepo.findById(userId).orElse(null);
            if(user.getAccessToken().equals(session.getAttribute("TOKEN")) && user.getRole().getDELETE_COMMENTS())
                return true;
        }
        return true;
    }
}