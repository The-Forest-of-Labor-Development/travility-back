package travility_back.travility.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import travility_back.travility.dto.oauth.CustomOAuthUser;
import travility_back.travility.security.jwt.JWTUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // OAuth2User
        CustomOAuthUser customUserDetails = (CustomOAuthUser) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority authority = iterator.next();
        String role = authority.getAuthority();

        if (role == null) {
            throw new IllegalArgumentException("Unknown role for user: " + username);
        }

        String token = jwtUtil.createJwt(username, role, 60 * 60 * 1000L); // 유효기간 60분

        // 토큰 전달방법 : 쿠키 전달
        response.addCookie(createCookie("Authorization", token));
        response.sendRedirect("http://localhost:3000/loading"); // 프론트단
    }

    // 쿠키 만드는 메서드
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60); //1시간
        cookie.setPath("/"); // 모든 경로에 대해 쿠키가 보임
        cookie.setHttpOnly(true); // JavaScript가 쿠키를 가져가지 못하게 httpOnly설정 (거의 필수인듯)

        return cookie;
    }


}
