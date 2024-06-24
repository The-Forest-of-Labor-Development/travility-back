package travility_back.travility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.entity.Member;
import travility_back.travility.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<MemberDTO> getMemberList() {
       return memberRepository.findAll()
               .stream()
               .map(member -> new MemberDTO(member))
               .collect(Collectors.toList());
    }
}
