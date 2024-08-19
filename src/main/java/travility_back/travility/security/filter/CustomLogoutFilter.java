package travility_back.travility.security.filter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.GenericFilterBean;
import travility_back.travility.repository.RefreshTokenRepository;
import travility_back.travility.security.jwt.JWTUtil;
import java.io.IOException;

public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public CustomLogoutFilter(JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        doFilter((HttpServletRequest)servletRequest, (HttpServletResponse)servletResponse,filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        if(!requestURI.matches("/api/logout")) { //로그아웃 요청이 아니면
            filterChain.doFilter(request, response);
            return;
        }

        String requestMethod = request.getMethod();
        if(!requestMethod.equals("POST")) { //POST 요청이 아니라면
            filterChain.doFilter(request, response);
            return;
        }

        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies) { //쿠키에서 Refresh Token 추출
            if(cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        if(refresh == null) { //Refresh Token이 null일 경우
            response.getWriter().write("refresh token null");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //Refresh Token 만료 여부
        try{
            jwtUtil.isExpired(refresh);
        }catch (ExpiredJwtException e){ //이미 로그아웃인 상태
            response.getWriter().write("refresh token expired");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //토큰 종류가 refresh인지 확인
        String category = jwtUtil.getCategory(refresh);
        if(!category.equals("refresh")){
            response.getWriter().write("invalid refresh token");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //토큰이 DB에 저장되어 있는지 확인
        Boolean isExist = refreshTokenRepository.existsByRefresh(refresh);
        if(!isExist){ //이미 로그아웃 상태
            response.getWriter().write("invalid refresh token");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //로그아웃
        //Refresh Token을 DB에서 제거
        refreshTokenRepository.deleteByRefresh(refresh);

        //쿠키 제거
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);

    }
}