package travility_back.travility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travility_back.travility.dto.ExpenseDTO;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Expense;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.ExpenseRepository;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AccountExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AccountBookRepository accountBookRepository;

    //지출 등록
    @Transactional
    public ExpenseDTO createExpense(ExpenseDTO expenseDTO) {
        AccountBook accountBook = accountBookRepository.findById(expenseDTO.getAccountBookId())
                .orElseThrow(() -> new NoSuchElementException("AccountBook not found"));

        Expense expense = new Expense();
        expense.setTitle(expenseDTO.getTitle());
        expense.setExpenseDate(expenseDTO.getExpenseDate());
        expense.setAmount(expenseDTO.getAmount());
        expense.setShared(expenseDTO.isShared());
        expense.setImgName(expenseDTO.getImgName());
        expense.setMemo(expenseDTO.getMemo());
        expense.setAccountBook(accountBook);
        expense.setPaymentMethod(expenseDTO.getPaymentMethod());
        expense.setCategory(expenseDTO.getCategory());
        expense.setCurUnit(expenseDTO.getCurUnit());

        Expense savedExpense = expenseRepository.save(expense);

        expenseDTO.setId(savedExpense.getId());
        return expenseDTO;
    }
}