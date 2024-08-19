package travility_back.travility.service.accountbook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travility_back.travility.dto.accountbook.AccountBookDTO;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Budget;
import travility_back.travility.entity.Expense;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.BudgetRepository;
import travility_back.travility.repository.ExpenseRepository;
import travility_back.travility.util.CalcUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final AccountBookRepository accountBookRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    /**
     * 정산용 가계부 조회
     */
    @Transactional(readOnly = true)
    public AccountBookDTO getAccountBook(Long id) {
        AccountBook accountBook = accountBookRepository.findById(id).orElseThrow(()->new NoSuchElementException("AccountBook not found"));
        return new AccountBookDTO(accountBook);
    }


    /**
     * 통화 코드별 공동 경비 합계 & 가중 평균 환율
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTotalSharedExpensesAndExchangeRates(Long id) {
        //가계부 아이디를 가진 예산 목록 찾기
        List<Budget> budgets = budgetRepository.findByAccountBookId(id);

        //예산의 통화 코드별 가중 평균 환율 계산
        Map<String, Double> currencyToAvgExchangeRate = CalcUtil.calculateWeightedAverageExchangeRateByCurrency(budgets);

        //예산의 통화 코드별 공동 경비 합계
        Map<String, Double> totalSharedExpensesByCurrency = new HashMap<>();
        for(String currency : currencyToAvgExchangeRate.keySet()) {
            List<Expense> expenses = expenseRepository.findSharedExpensesByAccountBookIdAndCurUnit(id, currency); //해당 통화코드 공동 경비
            Double sum = CalcUtil.calculateTotalExpensesByCurrency(expenses, currencyToAvgExchangeRate);
            totalSharedExpensesByCurrency.put(currency,sum);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalSharedExpensesByCurrency", totalSharedExpensesByCurrency); //공동 경비 합계
        result.put("currencyToAvgExchangeRate", currencyToAvgExchangeRate); //통화 코드별 가중 평균 환율

        return result;
    }
}
