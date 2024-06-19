package travility_back.travility.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import travility_back.travility.dto.CustomOAuthUser;
import travility_back.travility.dto.NaverOAuth2LoginDto;
import travility_back.travility.entity.enums.Role;

import java.io.IOException;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = null;

        // 쿠키들을 쿠키 리스트에 담는다
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {

//            System.out.println(cookie.getName());
            if (cookie.getName().equals("Authorization")) {

                authorization = cookie.getValue();
            }
        }

        //Authorization 헤더 검증
        if (authorization == null) { // null이면 다음 필터로 넘김

            System.out.println("token null");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        String token = authorization;

        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) { // 토큰이 소멸이 되어서 false가 뜨면 다음 필터로 넘겨줌

            System.out.println("token expired");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        //토큰에서 username과 role 획득
        String username = jwtUtil.getUsername(token);
        Role role = jwtUtil.getRole(token);

        //userDTO를 생성하여 값 set
        NaverOAuth2LoginDto memberDto = new NaverOAuth2LoginDto();
        memberDto.setUsername(username);
        memberDto.setRole(role);

        //UserDetails에 회원 정보 객체 담기
        CustomOAuthUser customOAuth2User = new CustomOAuthUser(memberDto);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);

    }
}
