package travility_back.travility.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import travility_back.travility.dto.ExpenseDTO;
import travility_back.travility.service.AccountExpenseService;

@RestController
@RequestMapping("/api/accountbook/expense")
@RequiredArgsConstructor
public class AccountExpenseController {

    private final AccountExpenseService accountExpenseService;

    @PostMapping
    public ExpenseDTO createExpense(@RequestBody ExpenseDTO expenseDTO) {
        return accountExpenseService.createExpense(expenseDTO);
    }
}
