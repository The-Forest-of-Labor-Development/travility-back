package travility_back.travility.service.statistic;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import travility_back.travility.dto.statistics.*;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Budget;
import travility_back.travility.entity.Expense;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Category;
import travility_back.travility.entity.enums.PaymentMethod;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.BudgetRepository;
import travility_back.travility.repository.ExpenseRepository;
import travility_back.travility.repository.MemberRepository;
import travility_back.travility.util.CalcUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final ExpenseRepository expenseRepository;
    private final MemberRepository memberRepository;
    private final AccountBookRepository accountBookRepository;

    //마이 리포트
    //카테고리별 지출 초기화
    private Map<String, Double> initializeCategoryToTotalAmount() {
        Map<String, Double> categoryToTotalAmount = new HashMap<>();
        for (Category category : Category.values()) {
            categoryToTotalAmount.put(category.toString(), 0.0);
        }
        return categoryToTotalAmount;
    }

    //결제방법별 지출 초기화
    private Map<String, Double> initializePaymentMethodToTotalAmount() {
        Map<String, Double> paymentMethodToTotalAmount = new HashMap<>();
        for (PaymentMethod paymentMethod : PaymentMethod.values()) {
            paymentMethodToTotalAmount.put(paymentMethod.toString(), 0.0);
        }
        return paymentMethodToTotalAmount;
    }

    //카테고리별 지출
    public Map<String, Double> calculateExpenseByCategory(List<Expense> expenses, Map<String, Double> currencyToAvgExchangeRate) {
        Map<String, Double> categoryToTotalAmount = initializeCategoryToTotalAmount();
        for (Expense expense : expenses) {
            String currency = expense.getCurUnit(); //통화 코드
            String category = expense.getCategory().toString(); //카테고리
            double amountInKRW = currencyToAvgExchangeRate.get(currency) * expense.getAmount(); //원화 지출
            categoryToTotalAmount.put(category, categoryToTotalAmount.getOrDefault(category, 0.0) + amountInKRW); //카테고리별 지출
        }
        return categoryToTotalAmount;
    }

    //결제방법별 지출
    public Map<String, Double> calculateExpenseByPaymentMethod(List<Expense> expenses, Map<String, Double> currencyToAvgExchangeRate) {
        Map<String, Double> paymentMethodToTotalAmount = initializePaymentMethodToTotalAmount();
        for (Expense expense : expenses){
            String currency = expense.getCurUnit(); //통화 코드
            String paymentMethod = expense.getPaymentMethod().toString();
            double amountInKRW = currencyToAvgExchangeRate.get(currency) * expense.getAmount(); //원화 지출
            paymentMethodToTotalAmount.put(paymentMethod, paymentMethodToTotalAmount.getOrDefault(paymentMethod, 0.0) + amountInKRW);
        }
        return paymentMethodToTotalAmount;
    }

    //마이 리포트 데이터
    public Map<String, Object> getMyReportData(String username){
        //회원 찾기
        Member member = memberRepository.findByUsername(username).orElseThrow(()-> new UsernameNotFoundException("Member Not found"));

        //해당 회원이 가진 가계부 리스트
        List<AccountBook> accountBooks = accountBookRepository.findByMemberId(member.getId());

        double totalExpenditure = 0; //총 누적 지출
        Map<String, Double> expenditureByCategory = initializeCategoryToTotalAmount(); //카테고리별 지출
        Map<String, Double> expenditureByPaymentMethod = initializePaymentMethodToTotalAmount();; //결제방법별 지출

        for (AccountBook accountBook : accountBooks){
            //해당 가계부 가중 평균 환율
            Map<String, Double> currencyToAvgExchangeRate = CalcUtil.calculateWeightedAverageExchangeRateByCurrency(accountBook.getBudgets());

            //해당 가계부 내 모든 지출 내역
            List<Expense> expenses = accountBook.getExpenses();

            //해당 가계부 카테고리별 지출
            Map<String, Double> categoryExpenses = calculateExpenseByCategory(expenses, currencyToAvgExchangeRate); //해당 가계부 카테고리별 지출
            for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()){
                expenditureByCategory.put(entry.getKey(), expenditureByCategory.get(entry.getKey()) + entry.getValue());
            }

            //해당 가계부 결제방법별 지출
            Map<String, Double> paymentMethodExpenses = calculateExpenseByPaymentMethod(expenses, currencyToAvgExchangeRate); //해당 가계부 결제방법별 지출
            for (Map.Entry<String, Double> entry : paymentMethodExpenses.entrySet()){
                expenditureByPaymentMethod.put(entry.getKey(), expenditureByPaymentMethod.get(entry.getKey()) + entry.getValue());
            }

            //해당 가계부 총 누적 지출
            totalExpenditure += CalcUtil.calculateTotalExpenses(accountBook);
        }

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("totalExpenditure", totalExpenditure);
        reportData.put("expenditureByCategory", expenditureByCategory);
        reportData.put("expenditureByPaymentMethod", expenditureByPaymentMethod);

        return reportData;
    }



    //지출 통계

    //총 지출
    public double getTotalExpenditureByAccountBook(Long accountBookId){
        AccountBook accountBook = accountBookRepository.findById(accountBookId).orElseThrow(()-> new NoSuchElementException("AccountBook not found"));
        return CalcUtil.calculateTotalExpenses(accountBook);
    }

    //총 예산
    public double getTotalBudgetByAccountBook(Long accountBookId) {
        AccountBook accountBook = accountBookRepository.findById(accountBookId).orElseThrow(()-> new NoSuchElementException("AccountBook not found"));
        Map<String, Double> currencyToAvgExchangeRate = CalcUtil.calculateWeightedAverageExchangeRateByCurrency(accountBook.getBudgets()); //통화 코드 별 가중 평균 환율

        double totalBudget = 0;
        for (Budget budget : accountBook.getBudgets()){
            String currency = budget.getCurUnit(); //통화 코드
            double amount = budget.getAmount(); //예산 금액
            totalBudget += currencyToAvgExchangeRate.get(currency) * amount;
        }

        return  totalBudget;
    }

    //총 카테고리별 지출
    public Map<String, Double> getTotalExpenditureByAccountBookAndCategory(Long accountBookId){
        //가계부 찾기
        AccountBook accountBook = accountBookRepository.findById(accountBookId).orElseThrow(()-> new NoSuchElementException("AccountBook not found"));

        //가중 평균 환율
        Map<String, Double> currencyToAvgExchangeRate = CalcUtil.calculateWeightedAverageExchangeRateByCurrency(accountBook.getBudgets());

        //카테고리별 지출
        return calculateExpenseByCategory(accountBook.getExpenses(), currencyToAvgExchangeRate); //해당 날짜의 카테고리별 지출
    }


    //일자별 통계 (지출 항목)
    public List<DateCategoryAmountDTO> getDailyCategoryExpense(Long accountBookId){
        //가계부 찾기
        AccountBook accountBook = accountBookRepository.findById(accountBookId).orElseThrow(()-> new NoSuchElementException("AccountBook not found"));

        //가중 평균 환율
        Map<String, Double> currencyToAvgExchangeRate = CalcUtil.calculateWeightedAverageExchangeRateByCurrency(accountBook.getBudgets());

        List<DateCategoryAmountDTO> dateCategoryAmountDTOs = new ArrayList<>();

        for(Expense expense : accountBook.getExpenses()){
            String currency = expense.getCurUnit(); //통화 코드
            double amount = expense.getAmount();
            double amountInKRW = currencyToAvgExchangeRate.get(currency) * amount;

            DateCategoryAmountDTO dateCategoryAmountDTO = new DateCategoryAmountDTO();
            dateCategoryAmountDTO.setDate(expense.getExpenseDate().toString());
            dateCategoryAmountDTO.setCategory(expense.getCategory());
            dateCategoryAmountDTO.setAmount(amountInKRW);

            dateCategoryAmountDTOs.add(dateCategoryAmountDTO);
        }

        return dateCategoryAmountDTOs;

    }

    //일자별 통계(결제 방법)
    public List<PaymentMethodAmountDTO> getDailyPaymentMethodExpense(Long accountBookId, String date){
        //가계부 찾기
        AccountBook accountBook = accountBookRepository.findById(accountBookId).orElseThrow(()-> new NoSuchElementException("AccountBook not found"));

        //해당 날짜 지출 목록
        LocalDate expenseDate = LocalDate.parse(date);
        LocalDateTime startOfDay = expenseDate.atStartOfDay();
        LocalDateTime endOfDay = expenseDate.plusDays(1).atStartOfDay();
        List<Expense> expenses = expenseRepository.findDailyAmountByPaymentMethod(accountBookId, startOfDay, endOfDay);

        //가중 평균 환율
        Map<String, Double> currencyToAvgExchangeRate = CalcUtil.calculateWeightedAverageExchangeRateByCurrency(accountBook.getBudgets());

        List<PaymentMethodAmountDTO> paymentMethodAmountDTOs = new ArrayList<>();

        double card = 0;
        double cash = 0;
        for(Expense expense : expenses){
            String currency = expense.getCurUnit();
            double amount = expense.getAmount();
            double amountInKRW = currencyToAvgExchangeRate.get(currency) * amount;

            if (expense.getPaymentMethod().toString().equals("CARD")){ //카드일 경우
                card += amountInKRW;
            }else{ //현금일 경우
                cash += amountInKRW;
            }
        }

        PaymentMethodAmountDTO cardDTO = new PaymentMethodAmountDTO();
        PaymentMethodAmountDTO cashDTO = new PaymentMethodAmountDTO();

        //카드
        cardDTO.setDate(date);
        cardDTO.setPaymentMethod(PaymentMethod.CARD);
        cardDTO.setAmount(card);

        paymentMethodAmountDTOs.add(cardDTO);

        //현금
        cashDTO.setDate(date);
        cashDTO.setPaymentMethod(PaymentMethod.CASH);
        cashDTO.setAmount(cash);

        paymentMethodAmountDTOs.add(cashDTO);

        return paymentMethodAmountDTOs;
    }

    //라인 차트 (카테고리)
    public List<DateCategoryAmountDTO> getDailyCategoryExpenseForLineChart(Long accountBookId, String category){
        //가계부 찾기
        AccountBook accountBook = accountBookRepository.findById(accountBookId).orElseThrow(()-> new NoSuchElementException("AccountBook not found"));

        //가중 평균 환율
        Map<String, Double> currencyToAvgExchangeRate = CalcUtil.calculateWeightedAverageExchangeRateByCurrency(accountBook.getBudgets());

        List<DateCategoryAmountDTO> dateCategoryAmountDTOS = new ArrayList<>();

        if (category.equals("ALL")){
            return getDailyCategoryExpense(accountBookId);
        }else{
            List<Expense> expenses = expenseRepository.findDailyAmountByCategoryForLineChart(accountBookId, Category.valueOf(category));

            for (Expense expense : expenses){
                String currency = expense.getCurUnit();
                double amount = expense.getAmount();
                double amountInKRW = currencyToAvgExchangeRate.get(currency) * amount;

                DateCategoryAmountDTO dateCategoryAmountDTO = new DateCategoryAmountDTO();

                dateCategoryAmountDTO.setDate(expense.getExpenseDate().toString());
                dateCategoryAmountDTO.setCategory(expense.getCategory());
                dateCategoryAmountDTO.setAmount(amountInKRW);

                dateCategoryAmountDTOS.add(dateCategoryAmountDTO);
            }

        }
        return dateCategoryAmountDTOS;
    }

}