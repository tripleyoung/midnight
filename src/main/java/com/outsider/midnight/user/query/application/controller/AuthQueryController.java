package com.outsider.midnight.user.query.application.controller;

import com.outsider.midnight.user.command.application.dto.LoginRequestDTO;
import com.outsider.midnight.user.command.application.dto.UserInfoRequestDTO;
import com.outsider.midnight.user.command.application.service.AuthService;
import com.outsider.midnight.user.command.infrastructure.service.CustomUserDetail;
import com.outsider.midnight.user.query.application.service.UserInfoService;
import com.outsider.midnight.util.UserId;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;
@Hidden
@RestController
@RequestMapping("/api/v1/auth")
public class AuthQueryController {

    private final AuthService authService;
    private final UserInfoService userService;
    public AuthQueryController(AuthService authService, UserInfoService userService) {
        this.authService = authService;
        this.userService = userService;
    }
    @CrossOrigin(origins = "https://monitor.master-of-prediction.shop:3001", allowedHeaders = "*",allowCredentials = "true")
    @PostMapping("login")
    public  ResponseEntity<UserInfoRequestDTO> postMemberProfile(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse response
    ) throws IOException {

        String token = this.authService.login(request);

        // 쿠키 생성
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(false); // 클라이언트 측에서 자바스크립트로 접근 불가능하게 설정
        cookie.setSecure(true); // HTTPS 환경에서만 전송되도록 설정 (HTTPS를 사용하는 경우)
        cookie.setDomain("master-of-prediction.shop"); // 쿠키가 유효한 경로 설정
        cookie.setPath("/"); // 쿠키가 유효한 경로 설정
        cookie.setAttribute("SameSite","None");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 쿠키 유효 기간 설정 (7일 예시)
        response.addCookie(cookie);
        // 사용자 정보 DTO 생성
//        response.sendRedirect("https://monitor.master-of-prediction.shop:3001");
        // 리다이렉트
        // HTTP 200 OK 상태 코드 반환
        // HTTP 200 OK 상태 코드 설정
//        response.setStatus(HttpServletResponse.SC_OK);
        String email = request.getEmail();
        Optional<UserInfoRequestDTO> userInfo = userService.getUserInfoByEmail(email);
        if (userInfo.isPresent()) {
            userInfo.get().setToken(token);
            return ResponseEntity.ok(userInfo.get());
        }else
        {
            return ResponseEntity.notFound().build();
        }

    }
    @GetMapping("users")
    public  ResponseEntity<UserInfoRequestDTO> getMemberProfile(@UserId Long userId
    ) throws IOException {
        System.out.println(userId);
        Optional<UserInfoRequestDTO> userInfo = userService.getUserInfoById(userId);
        if (userInfo.isPresent()) {
            return ResponseEntity.ok(userInfo.get());
        }else
        {
            return ResponseEntity.notFound().build();
        }

    }

}
