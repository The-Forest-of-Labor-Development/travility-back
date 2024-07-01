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
import travility_back.travility.service.statistic.StatisticService;

@RestController
@RequestMapping("/api/accountbook")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

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
}
