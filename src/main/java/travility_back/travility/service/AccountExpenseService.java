package travility_back.travility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import travility_back.travility.dto.ExpenseDTO;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Expense;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.ExpenseRepository;

@Service
@RequiredArgsConstructor
public class AccountExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AccountBookRepository accountBookRepository;

    public ExpenseDTO createExpense(ExpenseDTO expenseDTO) {
        AccountBook accountBook = accountBookRepository.findById(expenseDTO.getAccountBookId())
                .orElseThrow(() -> new RuntimeException("Account book not found"));

        Expense expense = new Expense();
        expense.setExpenseDate(expenseDTO.getExpenseDate());
        expense.setAmount(expenseDTO.getAmount());
        expense.setShared(expenseDTO.isShared());
        expense.setImgName(expenseDTO.getImgName());
        expense.setMemo(expenseDTO.getMemo());
        expense.setAccountBook(accountBook);
        expense.setPaymentMethod(expenseDTO.getPaymentMethod());
        expense.setCategory(expenseDTO.getCategory());

        Expense savedExpense = expenseRepository.save(expense);

        expenseDTO.setId(savedExpense.getId());
        return expenseDTO;
    }
}
