package travility_back.travility.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import travility_back.travility.dto.CustomOAuthUser;
import travility_back.travility.entity.enums.Role;
import travility_back.travility.jwt.JWTUtil;

import java.io.IOException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // OAuth2User
        CustomOAuthUser customUserDetails = (CustomOAuthUser) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Role 클래스로 권한을 설정해줬으니까 Role enum 타입으로 변환
        Role role = null;
        for (GrantedAuthority authority : authorities) {
            System.out.println("authority.getAuthority().toString() = " + authority.getAuthority().toString()); // 삭제
            if (authority.getAuthority().equals("ROLE_USER")) {
                role = Role.USER;
            } else if (authority.getAuthority().equals("ROLE_ADMIN")) {
                role = Role.ADMIN;
            }
        }

        if (role == null) {
            throw new IllegalArgumentException("Unknown role for user: " + username);
        }

        String token = jwtUtil.createJwt(username, role, 36000000L); // 유효기간 60분

        // 토큰 전달방법 : 쿠키 전달
        response.addCookie(createCookie("Authorization", token));
        response.sendRedirect("http://localhost:3000/"); // 프론트단
    }

    // 쿠키 만드는 메서드
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 60);
        cookie.setPath("/"); // 모든 경로에 대해 쿠키가 보임
        cookie.setHttpOnly(true); // JavaScript가 쿠키를 가져가지 못하게 httpOnly설정 (거의 필수인듯)

        return cookie;
    }


}
