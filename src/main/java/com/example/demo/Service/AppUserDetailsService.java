package com.example.demo.Service;

import com.example.demo.Auth.AppUserDetails;
import com.example.demo.Model.User;
import com.example.demo.Repo.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    public AppUserDetailsService(UserRepository userRepository) { this.userRepository = userRepository; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        if (username.contains("@")) {
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } else {
            user = userRepository.findByPhone(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }
        return new AppUserDetails(user);
    }
}
