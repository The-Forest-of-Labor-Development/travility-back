package travility_back.travility.service;

import jakarta.servlet.http.HttpServletRequest;
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
import travility_back.travility.entity.enums.Role;
import travility_back.travility.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
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
        System.out.println(memberDTO.getCreatedDate());
        String encodePassword = bCryptPasswordEncoder.encode(memberDTO.getPassword());
        memberDTO.setPassword(encodePassword);
        memberDTO.setCreatedDate(LocalDateTime.now());
        memberDTO.setRole(Role.ROLE_USER); // Role 타입으로 설정
        Member member = new Member(memberDTO);

        memberRepository.save(member);
    }

    //회원 정보
    @Transactional(readOnly = true)
    public Map<String, String> getMemberInfo(CustomUserDetails userDetails) {
        Member member = memberRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new NoSuchElementException("Member not found"));
        Map<String, String> map = new HashMap<>();
        map.put("username", member.getUsername());
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
                "&access_token=" + member.getAccessToken() +
                "&service_provider=NAVER";
        try {
            sendRevokeRequest(requestUrl, "NAVER", null);
            memberRepository.deleteById(member.getId());
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    //구글 회원 탈퇴
    @Transactional
    public void deleteGoogleAccount(CustomUserDetails userDetails) {
        Member member = memberRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new NoSuchElementException("Member not found"));
        String requestUrl = "https://accounts.google.com/o/oauth2/revoke?token=" + member.getAccessToken();
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
        headers.setBearerAuth(member.getAccessToken()); //Authorization: Bearer abc123

        try {
            sendRevokeRequest(requestUrl, "KAKAO", headers);
            memberRepository.deleteById(member.getId());
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        }

    }

    //서비스 제공자에 회원 탈퇴 요청
    public HttpStatus sendRevokeRequest(String requestUrl, String provider, HttpHeaders headers) {
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
