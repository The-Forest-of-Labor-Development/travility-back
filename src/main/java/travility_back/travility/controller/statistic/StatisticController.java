package travility_back.travility.controller.statistic;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import travility_back.travility.dto.auth.CustomUserDetails;
import travility_back.travility.dto.statistics.*;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Category;
import travility_back.travility.repository.MemberRepository;
import travility_back.travility.service.statistic.StatisticService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    /**
     * 마이 리포트
     */
    @GetMapping("/myreport")
    public Map<String, Object> getMyReportData(@AuthenticationPrincipal CustomUserDetails userDetails){
        return statisticService.getMyReportData(userDetails.getUsername());
    }


    /**
     * 총 누적 지출
     */
    @GetMapping("/total")
    public double getTotalExpenditure(@RequestParam Long accountBookId){
        return statisticService.getTotalExpenditureByAccountBook(accountBookId);
    }

    /**
     * 총 예산
     */
    @GetMapping("/total/budget")
    public double getTotalBudget(@RequestParam Long accountBookId){
        return statisticService.getTotalBudgetByAccountBook(accountBookId);
    }

    /**
     * 가계부 카테고리별 총 지출
     */
    @GetMapping("/total/category")
    public Map<String, Double> getExpenditureByCategory(@RequestParam Long accountBookId){
        return statisticService.getTotalExpenditureByAccountBookAndCategory(accountBookId);
    }

    /**
     * 일자별 통계(카테고리)
     */
    @GetMapping("/daily/category")
    public List<DateCategoryAmountDTO> getDailyCategoryExpense(@RequestParam Long accountBookId){
        return statisticService.getDailyCategoryExpense(accountBookId);
    }

    /**
     * 일자별 통계(결제 방법)
     */
    @GetMapping("/daily/paymentmethod")
    public List<PaymentMethodAmountDTO> getDailyPaymentMethodExpense(@RequestParam Long accountBookId, @RequestParam String date){
        return statisticService.getDailyPaymentMethodExpense(accountBookId, date);
    }

    /**
     * 라인 차트(카테고리)
     */
    @GetMapping("/daily/line-chart")
    public List<DateCategoryAmountDTO> getDailyCategoryExpenseForLineChart(@RequestParam Long accountBookId, @RequestParam String category){
        return statisticService.getDailyCategoryExpenseForLineChart(accountBookId, category);
    }


}