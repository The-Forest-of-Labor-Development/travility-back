package travility_back.travility.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.service.AdminService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

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

    /*여행 및 지출 관리*/

    //인기 여행지 Top5
    @GetMapping("/api/admin/travel/favorite-destinations")
    public List<String> getTop5TravelDestinations(){
        return adminService.getTop5TravelDestinations();
    }

   //지출 종류별 통계
}
