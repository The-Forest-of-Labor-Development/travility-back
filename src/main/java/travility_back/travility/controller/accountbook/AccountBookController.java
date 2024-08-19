package travility_back.travility.controller.accountbook;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import travility_back.travility.dto.accountbook.AccountBookDTO;
import travility_back.travility.dto.auth.CustomUserDetails;
import travility_back.travility.service.accountbook.AccountBookService;
import travility_back.travility.service.accountbook.BudgetService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/accountbook")
@RequiredArgsConstructor
public class AccountBookController {

    private final AccountBookService accountBookService;

    /**
     * 전체 가계부 조회(정렬)
     */
    @GetMapping("/accountbooks")
    public List<AccountBookDTO> getAllAccountBooks(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam(defaultValue = "new") String sort) {
        return accountBookService.getAllAccountBooks(userDetails.getUsername(), sort);
    }

    /**
     * 가계부 조회
     */
    @GetMapping("/{id}")
    public AccountBookDTO getAccountBookById(@PathVariable("id") Long id) {
        return accountBookService.getAccountBookById(id);
    }

    /**
     * 가계부 등록
     */
    @PostMapping
    public AccountBookDTO createAccountBook(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestBody AccountBookDTO accountBookDTO) {
        return accountBookService.createAccountBook(accountBookDTO, userDetails.getUsername());
    }

    /**
     * 가계부 정보 수정
     */
    @PutMapping("/{id}")
    public void updateAccountBook(@PathVariable Long id, @RequestPart(value = "tripInfo") String tripInfoString, @RequestPart(value="img", required = false) MultipartFile img) throws IOException {
        accountBookService.updateAccountBook(id, tripInfoString, img);
    }

    /**
     * 가계부 삭제
     */
    @DeleteMapping("/{id}")
    public void deleteAccountBook(@PathVariable Long id) {
        accountBookService.deleteAccountBook(id);
    }

    /**
     * 가계부 내보내기 (엑셀화)
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<?> exportAccountBookToExcel(@PathVariable Long id, @RequestParam boolean krw) {
        return accountBookService.exportAccountBookToExcel(id,krw);
    }

}