package travility_back.travility.service.member;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import travility_back.travility.dto.auth.CustomUserDetails;
import travility_back.travility.dto.member.MemberDTO;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Role;
import travility_back.travility.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.*;

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

    //회원가입
    @Transactional
    public void signup(MemberDTO memberDTO) {
        if (memberRepository.existsByUsername(memberDTO.getUsername())) { //중복 확인
            throw new DuplicateKeyException("Duplicate username");
        }
        memberDTO.setPassword(bCryptPasswordEncoder.encode(memberDTO.getPassword()));
        memberDTO.setCreatedDate(LocalDateTime.now());
        memberDTO.setRole(Role.ROLE_USER); // Role 타입으로 설정
        memberRepository.save(new Member(memberDTO));
    }

    //회원 정보
    @Transactional(readOnly = true)
    public Map<String, String> getMemberInfo(CustomUserDetails userDetails) {
        Member member = memberRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException("Member not found"));

        Map<String, String> memberInfo = new HashMap<>();
        memberInfo.put("username", member.getUsername());
        memberInfo.put("name", member.getName());
        memberInfo.put("email", member.getEmail());
        memberInfo.put("role", member.getRole().toString());
        memberInfo.put("socialType", member.getSocialType());
        memberInfo.put("createdDate", member.getCreatedDate().toString());

        return memberInfo;
    }

    @Transactional
    public void deleteMember(CustomUserDetails userDetails){
        Member member = memberRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException("Member not found"));
        String socialType = member.getSocialType();

        if(socialType  == null) {
            deleteStandardAccount(member.getId());
        }else{
            switch (socialType){
                case "naver":
                    deleteNaverAccount(member.getId(), member.getOauth2Token());
                    break;
                case "google":
                    deleteGoogleAccount(member.getId(), member.getOauth2Token());
                    break;
                case "kakao":
                    deleteKakaoAccount(member.getId(), member.getOauth2Token());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown social type: " + socialType);
            }
        }
    }

    //일반 회원 탈퇴
    private void deleteStandardAccount(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    //네이버 회원 탈퇴
    private void deleteNaverAccount(Long memberId, String oauth2Token) {
        String requestUrl = "https://nid.naver.com/oauth2.0/token?grant_type=delete" +
                "&client_id=" + naverClientId +
                "&client_secret=" + naverClientSecret +
                "&access_token=" + oauth2Token +
                "&service_provider=NAVER";
        sendRevokeRequest(requestUrl, HttpMethod.GET, null);
        memberRepository.deleteById(memberId);
    }

    //구글 회원 탈퇴
    private void deleteGoogleAccount(Long memberId, String oauth2Token) {
        String requestUrl = "https://accounts.google.com/o/oauth2/revoke?token=" + oauth2Token;
        sendRevokeRequest(requestUrl, HttpMethod.GET, null);
        memberRepository.deleteById(memberId);
    }

    //카카오 회원 탈퇴
    private void deleteKakaoAccount(Long memberId, String oauth2Token) { //카카오만 헤더에 액세스 토큰 담아서 전달
        String requestUrl = "https://kapi.kakao.com/v1/user/unlink";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(oauth2Token); //Authorization: Bearer abc123
        sendRevokeRequest(requestUrl, HttpMethod.POST, headers);
        memberRepository.deleteById(memberId);
    }

    //서비스 제공자에 회원 탈퇴 요청
    private void sendRevokeRequest(String requestUrl, HttpMethod method, HttpHeaders headers) {
        ResponseEntity<String> response;
        if (headers != null) {
            HttpEntity<String> entity = new HttpEntity<>(headers);
            response = restTemplate.exchange(requestUrl, method, entity, String.class);
        } else {
            response = restTemplate.exchange(requestUrl, method, null, String.class);
        }
        if (!response.getStatusCode().equals(HttpStatus.OK)) {
            throw new HttpClientErrorException(response.getStatusCode(), "Failed to revoke token");
        }
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
    }

}
