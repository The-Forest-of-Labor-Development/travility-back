package travility_back.travility.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import travility_back.travility.dto.CustomUserDetails;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Role;

import java.io.IOException;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")){
            System.out.println("token null"); //로그인 상태 아님
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7); //"Bearer " 이후부터 토큰

        if (jwtUtil.isExpired(token)){ //true면 token 만료
            System.out.println("token expired");
            throw new IllegalArgumentException("token 만료");
        }

        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setUsername(username);
        memberDTO.setRole(role.toUpperCase());
        memberDTO.setPassword("temppassword");

        CustomUserDetails customUserDetails = new CustomUserDetails(memberDTO);

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        filterChain.doFilter(request,response);

    }
}
