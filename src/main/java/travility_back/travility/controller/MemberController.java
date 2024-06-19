package travility_back.travility.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.service.MemberService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    //아이디 중복 확인
    @GetMapping("/api/duplicate-username")
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

    //로그아웃
}
