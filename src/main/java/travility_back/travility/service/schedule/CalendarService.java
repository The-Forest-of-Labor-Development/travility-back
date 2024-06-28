package travility_back.travility.service.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Member;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.MemberRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final AccountBookRepository accountBookRepository;
    private final MemberRepository memberRepository;

    public Long getMemberIdByUsername(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자 찾을 수 없음"));
        return member.getId();
    }

    public List<AccountBook> getAccountBooksByMemberId(Long memberId) {
        return accountBookRepository.findByMemberId(memberId);
    }

    public List<Map<String, Object>> getAccountBooksEventsByUsername(String username) {
        Long memberId = getMemberIdByUsername(username);
        List<AccountBook> accountBooks = getAccountBooksByMemberId(memberId);

        return accountBooks.stream().map(book -> {
            Map<String, Object> event = new HashMap<>();
            event.put("title", book.getTitle());
            event.put("start", book.getStartDate().toString()); // LocalDate로 변환
            event.put("end", book.getEndDate().toString()); // LocalDate로 변환
            return event;
        }).collect(Collectors.toList());
    }
}
