package travility_back.travility.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import travility_back.travility.dto.AccountBookDTO;
import travility_back.travility.dto.CustomUserDetails;
import travility_back.travility.service.AccountBookService;
import travility_back.travility.service.MemberService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accountbook")
@RequiredArgsConstructor
public class AccountBookController {

    private final AccountBookService accountBookService;
    private final MemberService memberService;

    //전체 가계부 조회
    @GetMapping("/accountbooks")
    public List<AccountBookDTO> getAllAccountBooks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        return accountBookService.getAllAccountBooks(username);
    }

    //가계부 조회
    @GetMapping("/{id}")
    public AccountBookDTO getAccountBookById(@PathVariable Long id) {
        return accountBookService.getAccountBookById(id);
    }

    //가계부 등록
    @PostMapping
    public AccountBookDTO saveAccountBook(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestBody AccountBookDTO accountBookDTO) {
        String username = userDetails.getUsername();
        return accountBookService.saveAccountBook(accountBookDTO, username);
    }

    //가계부 삭제
    @DeleteMapping("/{id}")
    public void deleteAccountBook(@PathVariable Long id) {
        accountBookService.deleteAccountBook(id);
    }

    //가계부 정보 수정
    @PutMapping("/{id}")
    public void updateAccountBook(@PathVariable Long id, @RequestPart("tripInfo") String tripInfoString, @RequestPart("img") MultipartFile img) throws IOException {
        accountBookService.updateAccountBook(id, tripInfoString, img);
    }
}