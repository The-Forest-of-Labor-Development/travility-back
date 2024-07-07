package travility_back.travility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travility_back.travility.dto.AccountBookDTO;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.ExpenseRepository;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final AccountBookRepository accountBookRepository;
    private final ExpenseRepository expenseRepository;

    //정산용 가계부 조회
    @Transactional(readOnly = true)
    public AccountBookDTO getAccountBook(Long id) {
        AccountBook accountBook = accountBookRepository.findById(id).orElseThrow(()->new NoSuchElementException("AccountBook not found"));
        return new AccountBookDTO(accountBook);
    }

    //공동 경비 합계
    @Transactional(readOnly = true)
    public Double getTotalSharedExpenses(Long id) {
        return expenseRepository.findTotalSharedExpensesByAccountBookId(id);
    }

    //1인당 정산 금액
    @Transactional(readOnly = true)
    public Double getPerPersonAmount(Long id) {
        AccountBook accountBook = accountBookRepository.findById(id).orElseThrow(()-> new NoSuchElementException("AccountBook not found"));
        int numberOfPeple = accountBook.getNumberOfPeople(); //인원수
        double totalSharedExpense = getTotalSharedExpenses(id); //공동 경비 합계
        return totalSharedExpense / numberOfPeple;
    }
}
