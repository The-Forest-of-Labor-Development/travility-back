package travility_back.travility.controller.accountbook;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import travility_back.travility.dto.accountbook.ExpenseDTO;
import travility_back.travility.service.accountbook.ExpenseService;

import java.io.IOException;

@RestController
@RequestMapping("/api/accountbook/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    //지출 등록
    @PostMapping
    public ExpenseDTO createExpense(@RequestPart(value = "expenseInfo") String expenseInfo, @RequestPart(value = "img", required = false) MultipartFile img) throws IOException {
        return expenseService.createExpense(expenseInfo, img);
    }

    //지출 수정
    @PutMapping("/{id}")
    public void updateExpense(@PathVariable Long id, @RequestPart(value = "expenseInfo") String expenseInfo, @RequestPart(value = "img", required = false) MultipartFile img) throws IOException {
        expenseService.updateExpense(id, expenseInfo, img);
    }

    //지출 삭제
    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
    }
}
