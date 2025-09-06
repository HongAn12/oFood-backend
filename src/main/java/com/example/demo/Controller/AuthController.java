package com.example.demo.Controller;

import com.example.demo.DTO.RegisterDtos.RegisterRequest;
import com.example.demo.DTO.RegisterDtos.UserResponse;
import com.example.demo.Service.JwtService;
import com.example.demo.Service.UserService;
import com.example.demo.DTO.AuthDtos.LoginRequest;
import com.example.demo.DTO.AuthDtos.RefreshRequest;
import com.example.demo.Auth.AppUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final AuthenticationManager authMgr;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authMgr, UserService userService, JwtService jwtService) {
        this.authMgr   = authMgr;
        this.userService = userService;
        this.jwtService  = jwtService;
    }


    /* ===================== 1) Đăng ký tài khoản ===================== */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        UserResponse user = userService.register(req);
        String access  = jwtService.generateAccess(user.id());
        String refresh = jwtService.generateRefresh(user.id());
        return ResponseEntity.ok(Map.of(
                "user", user,
                "accessToken", access,
                "refreshToken", refresh
        ));
    }

    /* ===================== 2) Đăng nhập email + password ===================== */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
        Authentication auth = authMgr.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );
        UserDetails principal = (UserDetails) auth.getPrincipal();
        AppUserDetails user   = (principal instanceof AppUserDetails a) ? a : null;

        String access  = jwtService.generateAccess(user.getId());
        String refresh = jwtService.generateRefresh(user.getId());

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "email", principal.getUsername(),
                "fullName", user.getFullName(),
                "roles", principal.getAuthorities(),
                "accessToken", access,
                "refreshToken", refresh
        ));
    }

    /* ===================== 3) Refresh access token ===================== */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody @Valid RefreshRequest req) {
        String token = req.refreshToken();
        if (!jwtService.isValid(token) || !jwtService.isRefreshToken(token)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid refresh token"));
        }
        Long userId = jwtService.parseUser(token);
        String access = jwtService.generateAccess(userId);
        return ResponseEntity.ok(Map.of("accessToken", access));
    }

    /* ===================== 4) Lấy thông tin user hiện tại ===================== */
    @GetMapping("/get-infor")
    public ResponseEntity<?> me(@AuthenticationPrincipal AppUserDetails user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "email", user.getUsername(),
                "fullName", user.getFullName(),
                "roles", user.getAuthorities()
        ));
    }
}
