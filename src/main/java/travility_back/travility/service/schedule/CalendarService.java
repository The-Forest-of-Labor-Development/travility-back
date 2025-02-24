package travility_back.travility.service.schedule;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import travility_back.travility.dto.accountbook.ExpenseDTO;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Budget;
import travility_back.travility.entity.Expense;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.BudgetRepository;
import travility_back.travility.repository.ExpenseRepository;
import travility_back.travility.repository.MemberRepository;
import travility_back.travility.util.CalcUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private static final Logger logger = LoggerFactory.getLogger(CalendarService.class);

    private final AccountBookRepository accountBookRepository;
    private final MemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    /**
     * username으로 id찾기
     */
    public Long getMemberIdByUsername(String username) {
        return memberRepository.findByUsername(username).map((member) -> member.getId()).orElseThrow(() -> new UsernameNotFoundException("Member not found"));
    }

    /**
     * 사용자 id로 그 사용자의 모든 account_book 조회
     */
    public List<AccountBook> getAccountBooksByMemberId(Long memberId) {
        return accountBookRepository.findByMemberId(memberId);
    }

    /**
     * username으로 일정(event) 가져오기
     */
    public List<Map<String, Object>> getAccountBooksEventsByUsername(String username) {
        Long memberId = getMemberIdByUsername(username);
        List<AccountBook> accountBooks = getAccountBooksByMemberId(memberId);

        // 예산 등록할 때 여러번 등록하면 같은 일정이 달력에 여러번 반복되어 출력됨. 그래서 Map으로 중복 제거
        Map<String, Map<String, Object>> uniqueEvents = new HashMap<>();

        // 일정(event) 생성
        for (AccountBook book : accountBooks) {
            // key : 시작날짜, 종료날짜
            String key = book.getStartDate().toString() + book.getEndDate().toString();
            if (!uniqueEvents.containsKey(key)) { // key가 중복되지 않았을 때만 일정을 추가해줌.
                Map<String, Object> event = new HashMap<>();
                event.put("accountbookId", book.getId()); //가계부 아이디
                event.put("title", book.getTitle()); // 제목 설정
                event.put("start", book.getStartDate().toString()); // 일정 시작 날짜 설정
                event.put("end", book.getEndDate().toString()); // 일정 종료 날짜 설정
                event.put("countryName", book.getCountryName()); //나라 이름
                event.put("imgName", book.getImgName()); //대표 이미지
                uniqueEvents.put(key, event); // Map에 넣어버리기
            }
        }

        // 추가한 일정을 리스트로 반환함
        return new ArrayList<>(uniqueEvents.values());
    }

    /**
     * 날짜별 지출 목록
     */
    public Map<LocalDate, Double> getExpenseByDay(Long id) {
        //id로 가계부 찾기
        logger.debug("Getting expenses by day for accountBookId: {}", id);
        AccountBook accountBook = accountBookRepository.findById(id).orElseThrow(() -> new NoSuchElementException("accountbook not found"));

        //가계부에서 여행 시작 날짜와 종료 날짜 추출
        LocalDate startDate = accountBook.getStartDate();
        LocalDate endDate = accountBook.getEndDate();
        //7.4 ~ 7.6

        //날짜와 총합을 저장할 map
        Map<LocalDate, Double> map = new HashMap<>();

        while (!startDate.isAfter(endDate)) {
            LocalDateTime start = startDate.atStartOfDay(); //현재 날짜 시작 00:00:00
            LocalDateTime end = startDate.plusDays(1).atStartOfDay(); //내일 날짜 시작 00:00:00
            Double sum = expenseRepository.findTotalAmountByDateRange(id, start, end); //해당 날짜의 총합 가져오기
            map.put(startDate, sum != null ? sum : 0.0); //추가 //2024-07-04 320000  //2024-07-05 400000
            logger.debug("Date: {}, Sum: {}", startDate, sum);
            startDate = startDate.plusDays(1);
        }
        return map;
    }

    /**
     * accountbookId 로 모든 expense 가져오기
     */
    public List<Expense> getAllExpensesByAccountbookId(Long accountbookId) {
        return expenseRepository.findByAccountBookId(accountbookId);
    }

    /**
     * 지출액 총합 계산(가중 평균)
     */
    public Map<String, Object> calculateTotalExpenses(Long id) {
        List<Budget> budgets = budgetRepository.findByAccountBookId(id); //가계부 예산
        List<Expense> expenses = expenseRepository.findByAccountBookId(id); //가계부 지출

        Map<String, Double> weightedAvgExchangeRates = CalcUtil.calculateWeightedAverageExchangeRateByCurrency(budgets); //가중 평균 환율
        List<ExpenseDTO> expenseDTOs = expenses.stream().map((expense -> new ExpenseDTO(expense))).collect(Collectors.toList()); //지출 목록
        double totalAmountKRW = CalcUtil.calculateTotalExpensesByCurrency(expenses, weightedAvgExchangeRates); //지출 총합

        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", totalAmountKRW);
        result.put("expenses", expenseDTOs);
        result.put("exchangeRates", weightedAvgExchangeRates);

        return result;
    }
}
