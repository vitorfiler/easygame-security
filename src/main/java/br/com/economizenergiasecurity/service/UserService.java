package br.com.economizenergiasecurity.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.economizenergiasecurity.repository.AuthorityRepository;
import br.com.economizenergiasecurity.repository.UserRepository;
import br.com.economizenergiasecurity.security.SecurityUtils;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthorityRepository authorityRepository;

//    @Autowired
//    private CacheManager cacheManager;

    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
                .flatMap(userRepository::findOneByLogin)
                .ifPresent(user -> {
                    String currentEncryptedPassword = user.getPassword();
                    if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                        throw new RuntimeException("Incorrect password");
                    }
                    String encryptedPassword = passwordEncoder.encode(newPassword);
                    user.setPassword(encryptedPassword);
//                    this.clearUserCaches(user);
                });
    }

//    private void clearUserCaches(User user) {
//        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evict((user.getLogin()));
//        if (user.getEmail() != null) {
//            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evict(user.getEmail());
//        }
//    }

}
