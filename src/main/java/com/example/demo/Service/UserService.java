package com.example.demo.Service;

import com.example.demo.DTO.RegisterDtos.RegisterRequest;
import com.example.demo.DTO.RegisterDtos.UserResponse;
import com.example.demo.Model.Store;
import com.example.demo.Model.User;
import com.example.demo.Model.UserRole;
import com.example.demo.Repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest req) {
        // unique checks
        if (repo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (req.phone() != null && !req.phone().isBlank() && repo.existsByPhone(req.phone())) {
            throw new IllegalArgumentException("Phone already in use");
        }

        User user = new User();
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setPhone(req.phone());
        user.setPasswordHash(encoder.encode(req.password()));
        user.setRole(UserRole.CUSTOMER); // mặc định
        user.setIsActive(true);

        // Nếu bạn đã có entity Store và muốn gán:
        if (req.storeId() != null) {
            Store s = new Store();
            s.setId(req.storeId());
            user.setStore(s); // chỉ set id, JPA sẽ tham chiếu lazy
        }

        user = repo.save(user);

        return new UserResponse(
                user.getId(), user.getFullName(), user.getEmail(), user.getPhone(),
                user.getRole().name(), user.getIsActive()
        );
    }
}
