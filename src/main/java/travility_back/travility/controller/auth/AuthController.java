package travility_back.travility.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import travility_back.travility.security.jwt.JWTUtil;
import travility_back.travility.service.auth.AuthService;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JWTUtil jwtUtil;

    //아이디 중복 확인
    @GetMapping("/duplicate-username")
    public boolean duplicateUsername(@RequestParam String username) {
        boolean isDuplicate = authService.duplicateUsername(username); //중복 여부
        if (isDuplicate) { //중복이라면
            return true;
        }
        return false;
    }

    //쿠키 JWT -> 헤더 JWT
    @GetMapping("/social-jwt")
    public void setAccessTokenFromRefreshToken(@CookieValue("refresh") String refreshToken, HttpServletResponse response) {
        if(refreshToken == null || refreshToken.isEmpty() || jwtUtil.isExpired(refreshToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //권한 없음
            return;
        }

        String username = jwtUtil.getUsername(refreshToken);
        String name = jwtUtil.getName(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        String accessToken = jwtUtil.createJwt("access",username,name,role,60 * 60 * 1000L);

        response.addHeader("Authorization", "Bearer " + accessToken);
    }

    //Access Token 재발급
    @PostMapping("/reissue")
    public ResponseEntity<?> reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) { //쿠키에 있는 리프레시 토큰 추출
            if (cookie.getName().equals("refresh")) {
                refreshToken = cookie.getValue();
            }
        }

        return authService.reissueAccessToken(refreshToken, response);
    }

    //토큰 만료 여부
    @GetMapping("/check-token")
    public boolean checkToken() { //토큰이 만료되었으면 jwt 필터에서 걸린다.
        return true;
    }

}
