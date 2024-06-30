package travility_back.travility.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import travility_back.travility.dto.AccountBookDTO;
import travility_back.travility.dto.CustomUserDetails;
import travility_back.travility.service.AccountBookService;
import travility_back.travility.service.MemberService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accountbook")
@RequiredArgsConstructor
public class AccountBookController {

    private final AccountBookService accountBookService;
    private final MemberService memberService;

    @GetMapping("/accountbooks")
    public List<AccountBookDTO> getAllAccountBooks() {
        return accountBookService.getAllAccountBooks();
    }

    @GetMapping("/{id}")
    public Optional<AccountBookDTO> getAccountBookById(@PathVariable("id") Long id) {
        return accountBookService.getAccountBookById(id);
    }

    @PostMapping
    public AccountBookDTO createAccountBook(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestBody AccountBookDTO accountBookDTO) {
        String username = userDetails.getUsername();
        Long memberId = memberService.findMemberId(username);
        accountBookDTO.getMember().setId(memberId);
        return accountBookService.saveAccountBook(accountBookDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteAccountBook(@PathVariable Long id) {
        accountBookService.deleteAccountBook(id);
    }
}