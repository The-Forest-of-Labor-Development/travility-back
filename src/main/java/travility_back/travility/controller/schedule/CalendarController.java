package travility_back.travility.controller.schedule;

import lombok.RequiredArgsConstructor;
//import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travility_back.travility.dto.CustomUserDetails;
import travility_back.travility.dto.ExpenseDTO;
import travility_back.travility.entity.Expense;
import travility_back.travility.service.schedule.CalendarService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accountBook")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/schedule")
    public ResponseEntity<List<Map <String, Object>>> getAccountBooksByMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        List<Map<String, Object>> events = calendarService.getAccountBooksEventsByUsername(username);
        return ResponseEntity.ok(events);
    }


    @GetMapping("/schedule/{id}")
    public ResponseEntity<Map<LocalDate, Double>> getExpenseByDay(@PathVariable("id") Long id) {
        Map<LocalDate, Double> expenses = calendarService.getExpenseByDay(id);
        return ResponseEntity.ok(expenses);
    }

    //accountbookId의 모든 지출 dto로 반환
    @GetMapping("/expenses/{accountbookId}")
    public ResponseEntity<List<ExpenseDTO>> getAllExpensesByAccountbookId(@PathVariable("accountbookId") Long accountbookId) {
        List<Expense> expenses = calendarService.getAllExpensesByAccountbookId(accountbookId);
        List<ExpenseDTO> expenseDTOs = expenses.stream()
                .map(ExpenseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(expenseDTOs);
    }

    @GetMapping("/{id}/totalExpenses")
    public ResponseEntity<Map<String, Object>> getTotalExpenses(@PathVariable("id") Long id) {
        Map<String, Object> result = calendarService.calculateTotalExpenses(id);
        return ResponseEntity.ok(result);
    }

}
