package travility_back.travility.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import travility_back.travility.dto.CustomUserDetails;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.service.MemberService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

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
