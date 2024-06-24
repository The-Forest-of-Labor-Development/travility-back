package travility_back.travility.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import travility_back.travility.dto.CustomUserDetails;
import travility_back.travility.dto.LoginDTO;
import travility_back.travility.security.jwt.JWTUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        LoginDTO loginDTO = null;
        ObjectMapper om = new ObjectMapper();

        try {
            loginDTO = om.readValue(request.getInputStream(), LoginDTO.class); //요청에 들어있는 JSON을 자바 객체로 역직렬화
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword(), null);

        return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
    }

    //로그인 인증 성공 후 로직
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authResult.getPrincipal(); //인증된 사용자 정보

        //username 추출
        String username = customUserDetails.getUsername();

        //role 추출
        Collection<? extends GrantedAuthority> collection = authResult.getAuthorities(); //권한 목록 반환
        Iterator<? extends GrantedAuthority> iterator = collection.iterator(); //반복자로 변환
        GrantedAuthority grantedAuthority = iterator.next(); //첫 번째 권한
        String role = grantedAuthority.getAuthority(); //권한 이름 반환


        String token = jwtUtil.createJwt(username, role,60 * 60 * 1000L); //1시간. 밀리초 단위

        response.setContentType("application/json"); //응답 타입 JSON
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Authorization", "Bearer " + token); //응답 헤더에 JWT 추가
    }

    //로그인 인증 실패 후 로직
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(401); //401 Unauthorized

        response.setContentType("application/json"); //응답 타입 JSON
        response.setCharacterEncoding("UTF-8");

    }
}
