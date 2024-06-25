package travility_back.travility.service.statistic;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import travility_back.travility.dto.statistics.MyReportExpenseStatisticsDTO;
import travility_back.travility.dto.statistics.PaymentMethodAmountDTO;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Category;
import travility_back.travility.entity.enums.PaymentMethod;
import travility_back.travility.repository.ExpenseRepository;
import travility_back.travility.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final MemberRepository memberRepository;

    // 사용자 통계 데이터 가져오기
    public MyReportExpenseStatisticsDTO getStatistics(Long memberId) {
        MyReportExpenseStatisticsDTO myReportExpenseStatisticsDto = new MyReportExpenseStatisticsDTO();

        // 지출 없어도 처리해서 뿌릴거에요
        List<Object[]> categoryAmounts = expenseRepository.findTotalAmountByCategory(memberId); // 카테고리별 총액 조회
        if (categoryAmounts.isEmpty()) { // 카테고리별 총액이 비어있으면
            // 모두 빈 값으로 초기화
            myReportExpenseStatisticsDto.setCategories(new String[]{});
            myReportExpenseStatisticsDto.setAmounts(new double[]{});
            myReportExpenseStatisticsDto.setPaymentMethods(new PaymentMethodAmountDTO[]{});
            myReportExpenseStatisticsDto.setTotalAmount(0);
            return myReportExpenseStatisticsDto;
        }

        // 카테고리와 각 카테고리별 지출액 반환
        String[] categories = new String[categoryAmounts.size()];
        double[] amounts = new double[categoryAmounts.size()];

        for (int i = 0; i < categoryAmounts.size(); i++) {
            categories[i] = ((Category) categoryAmounts.get(i)[0]).name(); // 카테고리 이름 설정
            amounts[i] = (double) categoryAmounts.get(i)[1]; // 해당 카테고리 지출액 설정
        }

        // 결제 방법별 총액 조회할거에요
        List<Object[]> paymentMethodAmounts = expenseRepository.findTotalAmountByPaymentMethod(memberId);
        PaymentMethodAmountDTO[] paymentMethods = new PaymentMethodAmountDTO[paymentMethodAmounts.size()];

        for (int i = 0; i < paymentMethodAmounts.size(); i++) {
            PaymentMethodAmountDTO paymentMethodAmountDTO = new PaymentMethodAmountDTO();
            paymentMethodAmountDTO.setPaymentMethod((PaymentMethod) paymentMethodAmounts.get(i)[0]); // 결제 방법 설정
            paymentMethodAmountDTO.setAmount((double) paymentMethodAmounts.get(i)[1]); // 해당 결제 방법의 지출액 설정
            paymentMethods[i] = paymentMethodAmountDTO;
        }

        // 전체 지출 총액 계산
        double totalAmount = 0;
        for (double amount : amounts) {
            totalAmount += amount; // 각 카테고리 지출액 전부 더한 총액
        }

        // dto 설정값 저장하고싶어요
        myReportExpenseStatisticsDto.setCategories(categories);
        myReportExpenseStatisticsDto.setAmounts(amounts);
        myReportExpenseStatisticsDto.setPaymentMethods(paymentMethods);
        myReportExpenseStatisticsDto.setTotalAmount(totalAmount);

        return myReportExpenseStatisticsDto;
    }

    /**
     * 현재 인증된(로그인된) 사용자의 지출 통계 데이터 가져오는 메서드
     * @return 현재 인증된 사용자의 지출 통계 데이터 DTO
     */
    public MyReportExpenseStatisticsDTO getStatistics() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Long memberId = getMemberIdByUsername(username); // 사용자 이름으로 username가져오기

        return getStatistics(memberId); // username으로 지출통계 가져오기
    }

    /**
     * 사용자 이름으로 username 가져오는 메서드
     * @param username 사용자 이름
     * @return 사용자 ID
     */
    private Long getMemberIdByUsername(String username) {
        Optional<Member> member = memberRepository.findByUsername(username); // 사용자 이름으로 사용자 검색
        if (member.isPresent()) {
            System.out.println("member.get().getId() = " + member.get().getId()); // 삭제
            return member.get().getId(); // 사용자 ID
        } else {
            throw new UsernameNotFoundException(username); // 사용자 없으면
        }
    }

    /**
     * 사용자 이름으로 사용자 객체 가져오는 메서드
     * @param username 사용자 이름
     * @return 사용자 객체
     */
    public Member getMemberByUsername(String username) {
        Optional<Member> member = memberRepository.findByUsername(username); // 사용자 이름으로 사용자 검색
        if (member.isPresent()) {
            return member.get(); // 객체 자체 반환
        } else {
            throw new UsernameNotFoundException(username); // 사용자 없으면
        }
    }

}
