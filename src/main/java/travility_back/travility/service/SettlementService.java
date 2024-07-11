package travility_back.travility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travility_back.travility.dto.AccountBookDTO;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Budget;
import travility_back.travility.entity.Expense;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.BudgetRepository;
import travility_back.travility.repository.ExpenseRepository;

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

    //정산용 가계부 조회
    @Transactional(readOnly = true)
    public AccountBookDTO getAccountBook(Long id) {
        AccountBook accountBook = accountBookRepository.findById(id).orElseThrow(()->new NoSuchElementException("AccountBook not found"));
        return new AccountBookDTO(accountBook);
    }

    //예산의 통화 코드별 가중 평균 환율
    public Map<String, Double> calculateWeightedAverageExchangeRateByCurrency(List<Budget> budgets) {
        Map<String, Double> currencyToAvgExchangeRate = new HashMap<>(); //통화 코드별 가중 평균 환율
        Map<String, Double> currencyToTotalAmount = new HashMap<>(); //통화 코드별 총 예산 금액

        for(Budget budget : budgets) {
            String currency = budget.getCurUnit(); //통화 코드
            double amount = budget.getAmount(); //예산 금액
            double exchangeRate = budget.getExchangeRate(); //환율

            currencyToTotalAmount.put(currency, currencyToTotalAmount.getOrDefault(currency, 0.0) + amount); //통화 코드별 예산 총 금액
            currencyToAvgExchangeRate.put(currency, currencyToAvgExchangeRate.getOrDefault(currency, 0.0) + exchangeRate * amount); //통화 코드별 가중 합
        }

        for(String currency : currencyToAvgExchangeRate.keySet()) {
            double totalAmount = currencyToTotalAmount.get(currency); //해당 통화 코드의 총 예산 금액
            currencyToAvgExchangeRate.put(currency, currencyToAvgExchangeRate.get(currency) / totalAmount); //가중 합 비운 후, 가중 합 / 총 예산 금액 -> 가중 평균 환율
        }

        return currencyToAvgExchangeRate;
    }

    //공동 경비 합계
    public Double calculateTotalSharedExpenses(List<Expense> expenses, Map<String, Double> currencyToAvgExchangeRate) {
        double totalSharedExpenses = 0.0;
        for (Expense expense : expenses) {
            String currency = expense.getCurUnit(); //통화 코드
            double amount = expense.getAmount(); //지출 금액
            double avgExchangeRate = currencyToAvgExchangeRate.get(currency); //해당 통화 코드 가중 평균 환율

            totalSharedExpenses += amount * avgExchangeRate;
        }
        return totalSharedExpenses;
    }

    //공동 경비 합계
    @Transactional(readOnly = true)
    public Map<String, Object> getTotalSharedExpensesAndExchangeRates(Long id) {
        //가계부 아이디 & 공유 경비인 지출 목록 찾기
        List<Expense> expenses = expenseRepository.findSharedExpensesByAccountBookId(id);

        //가계부 아이디를 가진 예산 목록 찾기
        List<Budget> budgets = budgetRepository.findByAccountBookId(id);

        // 예산의 통화 코드별 가중 평균 환율 계산
        Map<String, Double> currencyToAvgExchangeRate = calculateWeightedAverageExchangeRateByCurrency(budgets);

        //공동 경비 합계
        Double totalSharedExpenses = calculateTotalSharedExpenses(expenses, currencyToAvgExchangeRate);

        Map<String, Object> result = new HashMap<>();
        result.put("totalSharedExpenses", totalSharedExpenses); //공동 경비 합계
        result.put("currencyToAvgExchangeRate", currencyToAvgExchangeRate); //통화 코드별 가중 평균 환율

        return result;
    }

    //1인당 정산 금액
    @Transactional(readOnly = true)
    public Double getPerPersonAmount(Long id) {
        AccountBook accountBook = accountBookRepository.findById(id).orElseThrow(()-> new NoSuchElementException("AccountBook not found"));
        int numberOfPeple = accountBook.getNumberOfPeople(); //인원수
        Double totalSharedExpense = (Double) getTotalSharedExpensesAndExchangeRates(id).get("totalSharedExpenses"); //공동 경비 합계
        if(totalSharedExpense == null){
            totalSharedExpense = 0.0;
        }
        return totalSharedExpense / numberOfPeple;
    }
}
