package travility_back.travility.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import travility_back.travility.dto.auth.oauth2.CustomOAuthUser;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.RefreshToken;
import travility_back.travility.repository.MemberRepository;
import travility_back.travility.repository.RefreshTokenRepository;
import travility_back.travility.security.jwt.JWTUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // OAuth2User
        CustomOAuthUser customUserDetails = (CustomOAuthUser) authentication.getPrincipal();

        String username = customUserDetails.getUsername();
        String name = customUserDetails.getName();

        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority authority = iterator.next();
        String role = authority.getAuthority();

        if (role == null) {
            throw new IllegalArgumentException("Unknown role for user: " + username);
        }

        String refresh = jwtUtil.createJwt("refresh", username, name, role, 604800000L); // 일주일

        addRefreshToken(username,refresh,604800000L);

        // 토큰 전달방법 : 쿠키 전달
        response.addCookie(createCookie("refresh", refresh));
        response.sendRedirect("/loading"); // 프론트단
    }

    //Refresh 토큰 DB 저장
    private void addRefreshToken(String username, String refresh, Long expiredMs) {
        Member member = memberRepository.findByUsername(username).orElseThrow(()-> new NoSuchElementException("Member not found"));
        RefreshToken refreshToken = new RefreshToken(refresh,expiredMs.toString(),member);
        refreshTokenRepository.save(refreshToken);
    }

    // 쿠키 만드는 메서드
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(604800); //일주일
        cookie.setPath("/"); // 모든 경로에 대해 쿠키가 보임
        cookie.setHttpOnly(true); // JavaScript가 쿠키를 가져가지 못하게 httpOnly설정 (거의 필수인듯)

        return cookie;
    }


}
