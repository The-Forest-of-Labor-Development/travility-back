package travility_back.travility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Role;
import travility_back.travility.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    //아이디 중복 확인
    @Transactional
    public boolean duplicateUsername(String username){
        return memberRepository.existsByUsername(username);
    }

    //회원가입
    @Transactional
    public void signup(MemberDTO memberDTO){
        if (duplicateUsername(memberDTO.getUsername())){ //중복 확인
            throw new IllegalArgumentException("Duplicate username");
        }
        Member member = new Member();
        member.setUsername(memberDTO.getUsername());
        member.setPassword(bCryptPasswordEncoder.encode(memberDTO.getPassword()));
        member.setEmail(memberDTO.getEmail());
        member.setBirth(memberDTO.getBirth());
        member.setRole(Role.USER);
        member.setCreatedDate(memberDTO.getCreatedDate());

        memberRepository.save(member);
    }

}
