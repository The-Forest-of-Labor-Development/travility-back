package travility_back.travility;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.entity.Member;
import travility_back.travility.repository.MemberRepository;
import travility_back.travility.service.MemberService;

import java.time.LocalDateTime;

@SpringBootTest
class TravilityApplicationTests {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MemberService memberService;

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
