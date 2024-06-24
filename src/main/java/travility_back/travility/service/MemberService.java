package travility_back.travility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travility_back.travility.dto.CustomUserDetails;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Role;
import travility_back.travility.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        System.out.println(memberDTO.getCreatedDate());
        String encodePassword = bCryptPasswordEncoder.encode(memberDTO.getPassword());
        memberDTO.setPassword(encodePassword);
        memberDTO.setCreatedDate(LocalDateTime.now());
        memberDTO.setRole("ROLE_USER");
        Member member = new Member(memberDTO);

        memberRepository.save(member);
    }

    //회원 정보
    @Transactional
    public Map<String, String> getMemberInfo(CustomUserDetails member){
        Optional<Member> data = memberRepository.findByUsername(member.getUsername());
        Map<String, String> map = new HashMap<>();
        if(data.isPresent()){
            map.put("username", member.getUsername());
            map.put("email", data.get().getEmail());
            map.put("role", data.get().getRole().toString());
            map.put("socialType", data.get().getSocialType());
            map.put("createdDate", data.get().getCreatedDate().toString());
        }else{
            throw new IllegalArgumentException("User not found");
        }
        return map;
    }

}
