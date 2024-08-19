package travility_back.travility.controller.member;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travility_back.travility.dto.auth.CustomUserDetails;
import travility_back.travility.dto.member.LoginDTO;
import travility_back.travility.dto.member.MemberDTO;
import travility_back.travility.service.member.PasswordService;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    //비밀번호 찾기
    @PostMapping("/forgot-password")
    public void findPassword(@RequestBody MemberDTO memberDTO) throws MessagingException {
        passwordService.findPassword(memberDTO.getUsername(), memberDTO.getEmail());
    }

    //기존 비밀번호 확인
    @PostMapping("/confirm-password")
    public boolean confirmPassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody LoginDTO loginDTO) {
        return passwordService.confirmPassword(userDetails, loginDTO.getPassword());
    }

    //비밀번호 변경
    @PostMapping("/update-password")
    public void updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody LoginDTO loginDTO){
        passwordService.updatePassword(userDetails, loginDTO.getPassword());
    }
}
