package travility_back.travility.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import travility_back.travility.dto.ExpenseDTO;
import travility_back.travility.service.AccountExpenseService;

import java.io.IOException;

@RestController
@RequestMapping("/api/accountbook/expense")
@RequiredArgsConstructor
public class AccountExpenseController {

    private final AccountExpenseService accountExpenseService;

    //지출 등록
//    @PostMapping
//    public ExpenseDTO createExpense(@RequestBody ExpenseDTO expenseDTO) {
//        return accountExpenseService.createExpense(expenseDTO);
//    }
    @PostMapping
    public ExpenseDTO createExpense(@RequestPart(value = "expenseInfo") String expenseInfo, @RequestPart(value = "img", required = false) MultipartFile img) throws IOException {
        return accountExpenseService.createExpense(expenseInfo, img);
    }

    //지출 수정
    @PutMapping("/{id}")
    public void updateExpense(@PathVariable Long id, @RequestPart(value = "expense") String expenseInfo, @RequestPart(value = "img", required = false) MultipartFile img) throws IOException {
        accountExpenseService.updateExpense(id, expenseInfo, img);
    }
}
