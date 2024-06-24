package travility_back.travility.controller.statistic;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travility_back.travility.dto.statistics.MyReportExpenseStatisticsDTO;
import travility_back.travility.entity.Member;
import travility_back.travility.service.statistic.ExpenseService;

@RestController
@RequestMapping("/api/accountbook")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/detail")
    public ResponseEntity<MyReportExpenseStatisticsDTO> getStatistics() {
        MyReportExpenseStatisticsDTO statisticsDto = expenseService.getStatistics();
        return ResponseEntity.ok(statisticsDto);
    }

    @GetMapping("/userinfo")
    public ResponseEntity<Member> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Member member = expenseService.getMemberByUsername(username);
        return ResponseEntity.ok(member);
    }
}
