package travility_back.travility.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travility_back.travility.dto.member.MemberDTO;
import travility_back.travility.entity.enums.Role;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final AccountBookRepository accountBookRepository;

    //총 회원수
    @Transactional(readOnly = true)
    public long getTotalMembersCount() {
        return memberRepository.countByRole(Role.ROLE_USER);
    }

    //오늘 신규 가입자 수
    @Transactional(readOnly = true)
    public long getNewMembersCountToday(){
        LocalDate now = LocalDate.now(); //현재 날짜
        LocalDateTime startDate = now.atStartOfDay(); //현재 날짜 시작 00:00:00
        LocalDateTime endDate = now.plusDays(1).atStartOfDay(); //내일 날짜 시작 00:00:00
        return memberRepository.countNewMembersToday(startDate, endDate, Role.ROLE_USER);
    }

    //회원 페이징 처리
    @Transactional
    public List<MemberDTO> pagingMemberList(int page, int size, String sort){
        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdDate")); //생성일 기준 정렬

        return memberRepository.findAllByRole(Role.ROLE_USER, pageable)
                    .stream()
                    .map(member -> new MemberDTO(member))
                    .collect(Collectors.toList());

    }

    @Transactional
    public List<String> getTop5TravelDestinations() {
        Pageable pageable = PageRequest.of(0, 5); //첫 번째 페이지 5번째까지
        return accountBookRepository.findTop5TravelDestination(pageable);
    }

}
