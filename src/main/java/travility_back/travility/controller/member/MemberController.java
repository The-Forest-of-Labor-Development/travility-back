package travility_back.travility.controller.member;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import travility_back.travility.dto.auth.CustomUserDetails;
import travility_back.travility.dto.member.MemberDTO;
import travility_back.travility.service.member.MemberService;

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
        memberService.deleteMember(userDetails);
        memberService.logout(request,response);
    }

}
