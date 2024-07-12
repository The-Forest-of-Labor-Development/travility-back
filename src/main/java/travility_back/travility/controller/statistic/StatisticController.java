package travility_back.travility.controller.statistic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import travility_back.travility.dto.statistics.*;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Category;
import travility_back.travility.service.statistic.StatisticService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/accountbook")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    /**
     * 마이 리포트
     */
    @GetMapping("/detail")
    public ResponseEntity<MyReportExpenseStatisticsDTO> getStatistics() {
        MyReportExpenseStatisticsDTO statisticsDto = statisticService.getStatistics();
        return ResponseEntity.ok(statisticsDto);
    }

    @GetMapping("/userinfo")
    public ResponseEntity<Member> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Member member = statisticService.getMemberByUsername(username);
        return ResponseEntity.ok(member);
    }

    /**
     * 지출 통계
     */

    @GetMapping("/statistics/category")
    public ResponseEntity<List<DateCategoryAmountDTO>> getStatisticsByDate(@RequestParam Long accountBookId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long memberId = statisticService.getMemberIdByUsername(username);
        List<DateCategoryAmountDTO> statistics = statisticService.getStatisticsByDate(accountBookId, memberId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/paymentMethod")
    public ResponseEntity<List<PaymentMethodAmountDTO>> getPaymentMethodStatistics(@RequestParam Long accountBookId, @RequestParam LocalDate date) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long memberId = statisticService.getMemberIdByUsername(username);
        List<PaymentMethodAmountDTO> statistics = statisticService.getPaymentMethodStatistics(accountBookId, memberId, date);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/totalcategory")
    public ResponseEntity<List<DateCategoryAmountDTO>> getTotalAmountByCategoryForAll(@RequestParam Long accountBookId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long memberId = statisticService.getMemberIdByUsername(username);
        List<DateCategoryAmountDTO> statistics = statisticService.getTotalAmountByCategoryForAll(accountBookId, memberId);
        return ResponseEntity.ok(statistics);
    }

    //==예산 - 지출==//
    @GetMapping("/statistics/totalbudget")
    public ResponseEntity<Double> getTotalBudgetByAccountBookId(@RequestParam Long accountBookId) {
        Double totalBudget = statisticService.getTotalBudgetByAccountBookId(accountBookId);
        return ResponseEntity.ok(totalBudget);
    }

    @GetMapping("/statistics/totalexpense")
    public ResponseEntity<Double> getTotalExpenseByAccountBookId(@RequestParam Long accountBookId) {
        Double totalExpense = statisticService.getTotalExpenseByAccountBookId(accountBookId);
        return ResponseEntity.ok(totalExpense);
    }

    @GetMapping("/statistics/remaining-budget")
    public ResponseEntity<Double> getRemainingBudget(@RequestParam Long accountBookId) {
        Double remainingBudget = statisticService.getRemainingBudget(accountBookId);
        return ResponseEntity.ok(remainingBudget);
    }

    /**
     * 라인차트
     */
    @GetMapping("/statistics/category-by-dates")
    public ResponseEntity<List<DateCategoryAmountDTO>> getStatisticsByCategoryAndDates(@RequestParam Long accountBookId, @RequestParam String category) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long memberId = statisticService.getMemberIdByUsername(username);
        List<DateCategoryAmountDTO> statistics;

        if ("ALL".equals(category)) {
            statistics = statisticService.getStatisticsByDates(accountBookId, memberId);
        } else {
            List<Category> categories = Collections.singletonList(Category.valueOf(category));
            statistics = statisticService.getStatisticsByCategoryAndDates(accountBookId, memberId, categories);
        }

        return ResponseEntity.ok(statistics);
    }






//    @GetMapping("/statistics/categoryDateAmount")
//    public ResponseEntity<List<CategoryDateAmountDTO>> getCategoryAmountByDate(@RequestParam Long accountBookId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        Long memberId = statisticService.getMemberIdByUsername(username);
//        List<CategoryDateAmountDTO> statistics = statisticService.getCategoryAmountByDate(accountBookId, memberId, startDate, endDate);
//        return ResponseEntity.ok(statistics);

//    }
}
