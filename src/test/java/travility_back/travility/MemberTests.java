package travility_back.travility;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.repository.MemberRepository;
import travility_back.travility.service.MemberService;

@SpringBootTest
class MemberTests {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @Test
    public void signup(){
        //Given & When
        MemberDTO memberDTO = new MemberDTO();
        for(int i = 101; i<=130; i++){
            memberDTO.setUsername("testuser"+i);
            memberDTO.setPassword("testuser"+i+"!");
            memberDTO.setEmail("testuser"+i+"@example.com");
            memberService.signup(memberDTO);
        }
    }

    @Test
    public void testSignup(){
        //Given & When
        MemberDTO memberDTO = new MemberDTO();
        for(int i = 101; i<=130; i++){
            memberDTO.setUsername("testuser"+i);
            memberDTO.setPassword("testuser"+i+"!");
            memberDTO.setEmail("testuser"+i+"@example.com");
            memberService.signup(memberDTO);
        }
    }
}
