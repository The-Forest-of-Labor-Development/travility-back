package travility_back.travility.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import travility_back.travility.dto.CustomUserDetails;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.entity.Member;
import travility_back.travility.repository.MemberRepository;
import travility_back.travility.service.AdminService;
import travility_back.travility.service.MemberService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    /*사용자 관리*/

    //전체 회원 수
    @GetMapping("/users/total-count")
    public long getTotalMembersCount() {
        return adminService.getTotalMembersCount();
    }

    //신규 가입자 수
    @GetMapping("/users/new-today")
    public long getNewMembersCountToday() {
        return adminService.getNewMembersCountToday();
    }

    //회원 리스트 정렬, 페이징 처리
    @GetMapping("/users")
    public List<MemberDTO> pagingMemberList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "desc") String sort) {
        return adminService.pagingMemberList(page, size, sort);
    }

    //회원 계정 삭제
    @DeleteMapping("/users")
    public void deleteMember(@RequestBody String username) {
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new NoSuchElementException("Member not found"));
        MemberDTO memberDTO = new MemberDTO(member);
        CustomUserDetails userDetails = new CustomUserDetails(memberDTO);
        if (memberDTO.getSocialType().equals("kakao")) {
            memberService.deleteKakaoAccount(userDetails);
        } else if (memberDTO.getSocialType().equals("naver")) {
            memberService.deleteNaverAccount(userDetails);
        } else if (memberDTO.getSocialType().equals("google")) {
            memberService.deleteGoogleAccount(userDetails);
        } else {
            memberService.deleteStandardAccount(userDetails);
        }
    }

    /*여행 및 지출 관리*/

    //인기 여행지 Top5
    @GetMapping("/travel/favorite-destinations")
    public List<String> getTop5TravelDestinations() {
        return adminService.getTop5TravelDestinations();
    }

}
