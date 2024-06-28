package travility_back.travility.controller.schedule;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import travility_back.travility.dto.CustomUserDetails;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.service.schedule.CalendarService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accountBook")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/schedule")
    public ResponseEntity<List<Map<String, Object>>> getAccountBooksByMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        List<Map<String, Object>> events = calendarService.getAccountBooksEventsByUsername(username);
        return ResponseEntity.ok(events);
    }
}
