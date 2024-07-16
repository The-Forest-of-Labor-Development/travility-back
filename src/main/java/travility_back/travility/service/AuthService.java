package travility_back.travility.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.RefreshToken;
import travility_back.travility.repository.MemberRepository;
import travility_back.travility.repository.RefreshTokenRepository;
import travility_back.travility.security.jwt.JWTUtil;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    //아이디 중복 확인
    @Transactional
    public boolean duplicateUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    //Access Token 재발급
    @Transactional
    public ResponseEntity<?> reissueAccessToken(String refreshToken, HttpServletResponse response) {
        //Refresh Token이 없다면
        if (refreshToken == null || refreshToken.isEmpty()) {
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST); //400
        }

        //Refresh Token이 만료되었다면
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            refreshTokenRepository.deleteByRefresh(refreshToken);
            response.addCookie(createRefreshCookie("refresh", "",0));
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST); //400
        }

        //페이로드의 카테고리 refresh 추출
        String category = jwtUtil.getCategory(refreshToken);

        //refresh가 아니라면
        if (!category.equals("refresh")) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        //Refresh Token의 DB 존재 여부
        Boolean isExist = refreshTokenRepository.existsByRefresh(refreshToken);
        if (!isExist) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        //Refresh Token에서 정보 추출
        String username = jwtUtil.getUsername(refreshToken);
        String name = jwtUtil.getName(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        //새 토큰 생성
        String newAccess = jwtUtil.createJwt("access", username, name, role, 60 * 60 * 1000L); //1시간
        String newRefresh = jwtUtil.createJwt("refresh", username, name, role, 604800000L); //일주일

        //기존 Refresh Token 삭제 후, 새 토큰 DB 저장
        refreshTokenRepository.deleteByRefresh(refreshToken);
        addRefreshToken(username, newRefresh, 604800000L);

        //응답
        response.setHeader("Authorization", "Bearer " + newAccess);
        response.addCookie(createRefreshCookie("refresh", newRefresh, 604800));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //Refresh 토큰 DB 저장
    private void addRefreshToken(String username, String refresh, Long expiredMs) {
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new NoSuchElementException("Member not found"));
        RefreshToken refreshToken = new RefreshToken(refresh, expiredMs.toString(), member);
        refreshTokenRepository.save(refreshToken);
    }

    //Refresh Token 담을 쿠키 생성
    private Cookie createRefreshCookie(String key, String value, int expiredSeconds) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(expiredSeconds); //일주일
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

}
