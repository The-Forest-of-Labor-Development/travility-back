package travility_back.travility.util;

import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Budget;
import travility_back.travility.entity.Expense;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalcUtil {

    //예산의 통화 코드별 가중 평균 환율
    public static Map<String, Double> calculateWeightedAverageExchangeRateByCurrency(List<Budget> budgets) {
        Map<String, Double> currencyToAvgExchangeRate = new HashMap<>(); //통화 코드별 가중 평균 환율
        Map<String, Double> currencyToTotalAmount = new HashMap<>(); //통화 코드별 총 예산 금액

        for (Budget budget : budgets) {
            String currency = budget.getCurUnit(); //통화 코드
            double amount = budget.getAmount(); //예산 금액
            double exchangeRate = budget.getExchangeRate().doubleValue(); //환율

            currencyToTotalAmount.put(currency, currencyToTotalAmount.getOrDefault(currency, 0.0) + amount); //통화 코드별 예산 총 금액
            currencyToAvgExchangeRate.put(currency, currencyToAvgExchangeRate.getOrDefault(currency, 0.0) + exchangeRate * amount); //통화 코드별 가중 합
        }

        for (String currency : currencyToAvgExchangeRate.keySet()) {
            double totalAmount = currencyToTotalAmount.get(currency); //해당 통화 코드의 총 예산 금액
            currencyToAvgExchangeRate.put(currency, currencyToAvgExchangeRate.get(currency) / totalAmount); //가중 합 비운 후, 가중 합 / 총 예산 금액 -> 가중 평균 환율
        }

        return currencyToAvgExchangeRate;
    }

    //(공동 or 개인) 경비 합계
    public static Double calculateTotalExpensesByCurrency(List<Expense> expenses, Map<String, Double> currencyToAvgExchangeRate) {
        double totalExpenses = 0.0;
        for (Expense expense : expenses) {
            String currency = expense.getCurUnit(); //통화 코드
            double amount = expense.getAmount(); //지출 금액
            double avgExchangeRate = currencyToAvgExchangeRate.get(currency); //해당 통화 코드 가중 평균 환율

            totalExpenses += amount * avgExchangeRate;
        }
        return totalExpenses;
    }

    //가계부별 지출 총합
    public static Double calculateTotalExpenses(AccountBook accountBook) {
        Map<String, Double> currencyToAvgExchangeRate = calculateWeightedAverageExchangeRateByCurrency(accountBook.getBudgets()); //통화 코드별 가중 평균 환율

        double totalExpenses = 0.0;
        for (Expense expense : accountBook.getExpenses()) {
            String currency = expense.getCurUnit(); //통화 코드
            double amount = expense.getAmount(); //지출 금액
            double avgExchangeRate = currencyToAvgExchangeRate.get(currency); //해당 통화 코드 가중 평균 환율

            totalExpenses += amount * avgExchangeRate;
        }
        return totalExpenses;
    }
}
