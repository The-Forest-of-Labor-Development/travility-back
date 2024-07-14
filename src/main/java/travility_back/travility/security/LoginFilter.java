package travility_back.travility.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
import travility_back.travility.entity.Member;
import travility_back.travility.entity.RefreshToken;
import travility_back.travility.repository.MemberRepository;
import travility_back.travility.repository.RefreshTokenRepository;
import travility_back.travility.security.jwt.JWTUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, MemberRepository memberRepository, RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.refreshTokenRepository = refreshTokenRepository;
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
        String name = customUserDetails.getName(); //닉네임

        //role 추출
        Collection<? extends GrantedAuthority> collection = authResult.getAuthorities(); //권한 목록 반환
        Iterator<? extends GrantedAuthority> iterator = collection.iterator(); //반복자로 변환
        GrantedAuthority grantedAuthority = iterator.next(); //첫 번째 권한
        String role = grantedAuthority.getAuthority(); //권한 이름 반환

        //토큰 생성
        String access = jwtUtil.createJwt("access", username, name, role,60 * 60 * 1000L); //1시간. 밀리초 단위
        String refresh = jwtUtil.createJwt("refresh", username, name, role,604800000L); //일주일. 밀리초 단위

        //Refresh 토큰 저장
        addRefreshToken(username, refresh, 604800000L);

        //응답
        response.setContentType("application/json"); //응답 타입 JSON
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Authorization", "Bearer " + access); //응답 헤더에 accessToken 추가
        response.addCookie(createCookie("refresh", refresh)); //응답 쿠키에 refreshToken 추가
        response.setStatus(HttpServletResponse.SC_OK);
    }

    //로그인 인증 실패 후 로직
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType("application/json"); //응답 타입 JSON
        response.setCharacterEncoding("UTF-8");

    }

    //Refresh 토큰 DB 저장
    private void addRefreshToken(String username, String refresh, Long expiredMs) {
        Member member = memberRepository.findByUsername(username).orElseThrow(()-> new NoSuchElementException("Member not found"));
        RefreshToken refreshToken = new RefreshToken(refresh,expiredMs.toString(),member);
        refreshTokenRepository.save(refreshToken);
    }

    private Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(604800); //일주일. 초단위
        cookie.setPath("/"); //쿠키가 적용될 범위
        cookie.setHttpOnly(true); //자바스크립트 접근 x

        return cookie;
    }
}
