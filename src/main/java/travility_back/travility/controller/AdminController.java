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
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    /*사용자 관리*/

    //전체 회원 수
    @GetMapping("/api/admin/users/total-count")
    public long getTotalMembersCount() {
        return adminService.getTotalMembersCount();
    }

    //신규 가입자 수
    @GetMapping("/api/admin/users/new-today")
    public long getNewMembersCountToday() {
        return adminService.getNewMembersCountToday();
    }

    //회원 리스트 정렬, 페이징 처리
    @GetMapping("/api/admin/users")
    public List<MemberDTO> pagingMemberList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "desc")String sort){
        return adminService.pagingMemberList(page, size, sort);
    }

    //회원 계정 삭제
    @DeleteMapping("/api/admin/users")
    public void deleteMember(@RequestBody String username) {
        Optional<Member> data = memberRepository.findByUsername(username);
        if (data.isPresent()) {
            MemberDTO memberDTO = new MemberDTO(data.get());
            CustomUserDetails customUserDetails = new CustomUserDetails(memberDTO);
            if(memberDTO.getSocialType() == null) {
                memberService.deleteStandardAccount(customUserDetails);
            } else if (memberDTO.getSocialType().equals("naver")) {
                memberService.deleteNaverAccount(customUserDetails);
            } else if (memberDTO.getSocialType().equals("google")) {
                memberService.deleteGoogleAccount(customUserDetails);
            } else{
                memberService.deleteKakaoAccount(customUserDetails);
            }
        }else {
            throw new IllegalArgumentException("User not found");
        }
    }

    /*여행 및 지출 관리*/

    //인기 여행지 Top5
    @GetMapping("/api/admin/travel/favorite-destinations")
    public List<String> getTop5TravelDestinations(){
        return adminService.getTop5TravelDestinations();
    }

   //지출 종류별 통계
}
