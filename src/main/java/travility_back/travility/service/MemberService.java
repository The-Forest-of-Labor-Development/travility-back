package travility_back.travility.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import travility_back.travility.dto.CustomUserDetails;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.RefreshToken;
import travility_back.travility.entity.enums.Role;
import travility_back.travility.repository.MemberRepository;
import travility_back.travility.repository.RefreshTokenRepository;
import travility_back.travility.security.jwt.JWTUtil;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    @Transactional
    public Long findMemberId(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found with username: " + username));
        return member.getId();
    }

    //아이디 중복 확인
    @Transactional
    public boolean duplicateUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    //회원가입
    @Transactional
    public void signup(MemberDTO memberDTO) {
        if (duplicateUsername(memberDTO.getUsername())) { //중복 확인
            throw new DuplicateKeyException("Duplicate username");
        }
        String encodePassword = bCryptPasswordEncoder.encode(memberDTO.getPassword());
        memberDTO.setPassword(encodePassword);
        memberDTO.setCreatedDate(LocalDateTime.now());
        memberDTO.setRole(Role.ROLE_USER); // Role 타입으로 설정
        Member member = new Member(memberDTO);

        memberRepository.save(member);
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


    //회원 정보
    @Transactional(readOnly = true)
    public Map<String, String> getMemberInfo(CustomUserDetails userDetails) {
        Member member = memberRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new NoSuchElementException("Member not found"));
        Map<String, String> map = new HashMap<>();
        map.put("username", member.getUsername());
        map.put("name", member.getName());
        map.put("email", member.getEmail());
        map.put("role", member.getRole().toString());
        map.put("socialType", member.getSocialType());
        map.put("createdDate", member.getCreatedDate().toString());
        return map;
    }

    //일반 회원 탈퇴
    @Transactional
    public void deleteStandardAccount(CustomUserDetails userDetails) {
        Member member = memberRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new NoSuchElementException("Member not found"));
        memberRepository.deleteById(member.getId());
    }

    //네이버 회원 탈퇴
    @Transactional
    public void deleteNaverAccount(CustomUserDetails userDetails) {
        Member member = memberRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new NoSuchElementException("Member not found"));
        String requestUrl = "https://nid.naver.com/oauth2.0/token?grant_type=delete" +
                "&client_id=" + naverClientId +
                "&client_secret=" + naverClientSecret +
                "&access_token=" + member.getOauth2Token() +
                "&service_provider=NAVER";
        try {
            sendRevokeRequest(requestUrl, "NAVER", null);
            memberRepository.deleteById(member.getId());
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getResponseBodyAsString());
        }

    }

    //구글 회원 탈퇴
    @Transactional
    public void deleteGoogleAccount(CustomUserDetails userDetails) {
        Member member = memberRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new NoSuchElementException("Member not found"));
        String requestUrl = "https://accounts.google.com/o/oauth2/revoke?token=" + member.getOauth2Token();
        try {
            sendRevokeRequest(requestUrl, "GOOGLE", null);
            memberRepository.deleteById(member.getId());
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    //카카오 회원 탈퇴
    @Transactional
    public void deleteKakaoAccount(CustomUserDetails userDetails) { //카카오만 헤더에 액세스 토큰 담아서 전달
        Member member = memberRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new NoSuchElementException("Member not found"));

        String requestUrl = "https://kapi.kakao.com/v1/user/unlink";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(member.getOauth2Token()); //Authorization: Bearer abc123

        try {
            sendRevokeRequest(requestUrl, "KAKAO", headers);
            memberRepository.deleteById(member.getId());
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        }

    }

    //서비스 제공자에 회원 탈퇴 요청
    private HttpStatus sendRevokeRequest(String requestUrl, String provider, HttpHeaders headers) {
        ResponseEntity<String> responseEntity = null;
        if (provider.equals("NAVER")) { //네이버
            responseEntity = restTemplate.getForEntity(requestUrl, String.class);

        } else if (provider.equals("GOOGLE")) { //구글
            responseEntity = restTemplate.getForEntity(requestUrl, String.class);

        } else { //카카오
            HttpEntity<String> entity = new HttpEntity<>(headers);
            responseEntity = restTemplate.exchange(requestUrl, HttpMethod.POST, entity, String.class);
        }

        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            throw new HttpClientErrorException(responseEntity.getStatusCode(), "Failed to revoke token for provider: " + provider);
        }

        System.out.println(responseEntity.getStatusCode());
        return (HttpStatus) responseEntity.getStatusCode();
    }

    //회원 탈퇴용 로그아웃
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                cookie.setPath("/"); //모든 경로에서 삭제
                cookie.setMaxAge(0); //유효 기간 0
                response.addCookie(cookie);
            }
        }

        request.getSession().invalidate(); //세션 무효화
        System.out.println("success logged out");
    }

    // 객체 갖고오기
    public Member getMemberByUsername(String username) {
        Optional<Member> member = memberRepository.findByUsername(username);
        return member.orElseThrow(() -> new IllegalArgumentException("다음의 이름을 찾을 수 없음. : " + username));
    }


//    public Long findMemberId(String username) {
//        Member member = memberRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
//        return member.getId();
//}
}
