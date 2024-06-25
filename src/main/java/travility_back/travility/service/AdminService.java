package travility_back.travility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Role;
import travility_back.travility.repository.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public long getTotalMembersCount() {
        return memberRepository.countByRole(Role.ROLE_USER);
    }

    @Transactional(readOnly = true)
    public long getNewMembersCountToday(){
        LocalDate now = LocalDate.now(); //현재 날짜
        LocalDateTime startDate = now.atStartOfDay(); //현재 날짜 시작 00:00:00
        LocalDateTime endDate = now.plusDays(1).atStartOfDay(); //내일 날짜 시작 00:00:00
        return memberRepository.countNewMembersToday(startDate, endDate, Role.ROLE_USER);
    }

    @Transactional
    public List<MemberDTO> pagingMemberList(int page, int size, String sort){
        Pageable pageable = PageRequest.of(page, size, sort.equals("asc") ? Sort.by("createdDate").ascending() : Sort.by("createdDate").descending());

        if(sort.equals("asc")){
            return memberRepository.findAllByRoleOrderByCreatedDateAsc(Role.ROLE_USER, pageable)
                    .stream()
                    .map(member -> new MemberDTO(member))
                    .collect(Collectors.toList());
        }else {
            return memberRepository.findAllByRoleOrderByCreatedDateDesc(Role.ROLE_USER, pageable)
                    .stream()
                    .map(member -> new MemberDTO(member))
                    .collect(Collectors.toList());
        }
    }
}
