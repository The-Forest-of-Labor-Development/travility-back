package travility_back.travility.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import travility_back.travility.dto.CustomUserDetails;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.entity.Member;
import travility_back.travility.security.jwt.JWTUtil;
import travility_back.travility.service.MemberService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JWTUtil jwtUtil;

    //아이디 중복 확인
    @GetMapping("/api/auth/duplicate-username")
    public boolean duplicateUsername(@RequestParam String username) {
        boolean isDuplicate = memberService.duplicateUsername(username); //중복 여부
        if (isDuplicate) { //중복이라면
            return true;
        }
        return false;
    }

    //회원가입
    @PostMapping("/api/signup")
    public void signup(@RequestBody MemberDTO memberDTO) {
        memberService.signup(memberDTO);
    }

    //쿠키 JWT -> 헤더 JWT
    @GetMapping("/api/auth/social-jwt")
    public void getTokenfromCookie(@CookieValue("Authorization") String token, HttpServletResponse response) {
        if(token == null || token.isEmpty()) {
            response.setStatus(401); //권한 없음
        }
        response.addHeader("Authorization", "Bearer " + token);
    }

    //로그아웃
    @PostMapping("/api/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookie = request.getCookies();
        System.out.println(cookie);
        if(cookie != null) { //쿠키 있음 -> 소셜 로그인 사용자
            for(Cookie c : cookie) {
                System.out.println("Cookie Name: " + c.getName());
                if (c.getName().equals("Authorization") || c.getName().equals("JSESSIONID")) {
                    c.setValue("");
                    c.setPath("/"); //모든 경로에서 삭제
                    c.setMaxAge(0); //유효 기간 0
                    response.addCookie(c);
                }
            }
        }

        request.getSession().invalidate(); //세션 무효화
        System.out.println("로그아웃 성공");
    }

    //토큰 만료 여부
    @GetMapping("/api/auth/check-token")
    public boolean checkToken() { //토큰이 만료되었으면 jwt 필터에서 걸린다.
        return true;
    }

    //회원 정보
    @GetMapping("/api/users")
    public Map<String, String> getMemberInfo(@AuthenticationPrincipal CustomUserDetails member) {
        return memberService.getMemberInfo(member);
    }
}
