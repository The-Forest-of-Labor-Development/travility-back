package travility_back.travility.controller;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import travility_back.travility.dto.CustomUserDetails;
import travility_back.travility.dto.LoginDTO;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.security.jwt.JWTUtil;
import travility_back.travility.service.MemberService;

import java.io.IOException;
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

    //쿠키 JWT -> 헤더 JWT
    @GetMapping("/api/auth/social-jwt")
    public void getTokenfromCookie(@CookieValue("refresh") String refreshToken, HttpServletResponse response) {
        if(refreshToken == null || refreshToken.isEmpty() || jwtUtil.isExpired(refreshToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //권한 없음
            return;
        }

        String username = jwtUtil.getUsername(refreshToken);
        String name = jwtUtil.getName(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        String accessToken = jwtUtil.createJwt("access",username,name,role,60 * 60 * 1000L);

        response.addHeader("Authorization", "Bearer " + accessToken);
    }

    //Access Token 재발급
    @PostMapping("/api/auth/reissue")
    public ResponseEntity<?> reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) { //쿠키에 있는 리프레시 토큰 추출
            if (cookie.getName().equals("refresh")) {
                refreshToken = cookie.getValue();
            }
        }

        return memberService.reissueAccessToken(refreshToken, response);
    }

    //토큰 만료 여부
    @GetMapping("/api/auth/check-token")
    public boolean checkToken() { //토큰이 만료되었으면 jwt 필터에서 걸린다.
        return true;
    }

    //회원가입
    @PostMapping("/api/signup")
    public void signup(@RequestBody MemberDTO memberDTO) {
        memberService.signup(memberDTO);
    }

    //회원 정보
    @GetMapping("/api/users")
    public Map<String, String> getMemberInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return memberService.getMemberInfo(userDetails);
    }

    //기존 비밀번호 확인
    @PostMapping("/api/users/confirm-password")
    public boolean confirmPassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody LoginDTO loginDTO) {
        return memberService.confirmPassword(userDetails, loginDTO.getPassword());
    }

    //비밀번호 변경
    @PostMapping("/api/users/update-password")
    public void updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody LoginDTO loginDTO, HttpServletResponse response) throws IOException {
        memberService.updatePassword(userDetails, loginDTO.getPassword(), response);
    }

    //회원 탈퇴
    @DeleteMapping("/api/users")
    public void deleteMember(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal CustomUserDetails userDetails) {
        System.out.println(userDetails.getUsername());
        String socialType = getMemberInfo(userDetails).get("socialType");
        System.out.println(socialType);

        if(socialType  == null) {//일반 로그인 사용자
            memberService.deleteStandardAccount(userDetails);
            memberService.logout(request, response);
        } else if (socialType.equals("naver")) {//네이버 로그인 사용자
            memberService.deleteNaverAccount(userDetails);
            memberService.logout(request, response);
        } else if (socialType.equals("google")) {//구글 로그인 사용자
            memberService.deleteGoogleAccount(userDetails);
            memberService.logout(request, response);
        } else if (socialType.equals("kakao")) { // 카카오
            memberService.deleteKakaoAccount(userDetails);
            memberService.logout(request, response);
        }
    }

}
