package travility_back.travility.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import travility_back.travility.dto.CustomUserDetails;
import travility_back.travility.dto.LoginDTO;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.service.PasswordService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    //비밀번호 찾기
    @PostMapping("/api/users/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody MemberDTO memberDTO){
        return passwordService.forgotPassword(memberDTO.getUsername(), memberDTO.getEmail());
    }

    //기존 비밀번호 확인
    @PostMapping("/api/users/confirm-password")
    public boolean confirmPassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody LoginDTO loginDTO) {
        return passwordService.confirmPassword(userDetails, loginDTO.getPassword());
    }

    //비밀번호 변경
    @PostMapping("/api/users/update-password")
    public void updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody LoginDTO loginDTO, HttpServletResponse response) throws IOException {
        passwordService.updatePassword(userDetails, loginDTO.getPassword(), response);
    }
}
