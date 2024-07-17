package travility_back.travility.service.statistic;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import travility_back.travility.dto.statistics.*;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Category;
import travility_back.travility.entity.enums.PaymentMethod;
import travility_back.travility.repository.BudgetRepository;
import travility_back.travility.repository.ExpenseRepository;
import travility_back.travility.repository.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final ExpenseRepository expenseRepository;
    private final MemberRepository memberRepository;
    private final BudgetRepository budgetRepository;

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
    public Long getMemberIdByUsername(String username) {
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
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 날짜별로 카테고리 지출 금액 가져오는 메서드
     */
    public List<DateCategoryAmountDTO> getStatisticsByDate(Long accountBookId, Long memberId) {
        List<Object[]> results = expenseRepository.findTotalAmountByDateAndCategory(accountBookId, memberId);
        return results.stream()
                .map(result -> new DateCategoryAmountDTO(
                        ((LocalDateTime) result[0]).toString(),
                        (Category) result[1],
                        (Double) result[2]
                ))
                .collect(Collectors.toList());
    }

    /**
     * 날짜별로 지출 방법별 금액 가져오기
     */
    public List<PaymentMethodAmountDTO> getPaymentMethodStatistics(Long accountBookId, Long memberId, LocalDateTime date) {
        List<Object[]> results = expenseRepository.findTotalAmountByPaymentMethodAndDate(accountBookId, memberId, date);
        return results.stream()
                .map(result -> new PaymentMethodAmountDTO(
                        (PaymentMethod) result[0],
                        (Double) result[1]
                ))
                .collect(Collectors.toList());
    }

    /**
     * 한 일정에 대한 카테고리별 총 지출 가져오기
     */
    public List<DateCategoryAmountDTO> getTotalAmountByCategoryForAll(Long accountBookId, Long memberId) {
        List<Object[]> results = expenseRepository.findTotalAmountByCategoryForAll(accountBookId, memberId);
        return results.stream()
                .map(result -> new DateCategoryAmountDTO(
                        "TOTAL",
                        (Category) result[0],
                        (Double) result[1]
                ))
                .collect(Collectors.toList());
    }

    // 예산 - 지출
    public Double getTotalBudgetByAccountBookId(Long accountBookId) {
        return budgetRepository.getTotalBudgetByAccountBookId(accountBookId);
    }

    public Double getTotalExpenseByAccountBookId(Long accountBookId) {
        return expenseRepository.getTotalExpenseByAccountBookId(accountBookId);
    }

    public Double getRemainingBudget(Long accountBookId) {
        Double totalBudget = getTotalBudgetByAccountBookId(accountBookId);
        Double totalExpense = getTotalExpenseByAccountBookId(accountBookId);
        return totalBudget - totalExpense;
    }

    /**
     * 라인차트
     * 사용자의 특정 가계부에 대한 날짜별 총 지출 금액 조회 (전체)
     */
    public List<DateCategoryAmountDTO> getStatisticsByDates(Long accountBookId, Long memberId) {
        List<Object[]> results = expenseRepository.findTotalAmountByDates(accountBookId, memberId); // 특정 가계부에 대한 날짜별 총 지출 금액 조회
        return results.stream() // 조회한 결과를 DTO객체로 변환 후 리스트형태로 반환
                .map(result -> new DateCategoryAmountDTO( // 맞나 모름
                        ((LocalDateTime) result[0]).toString(),
                        null, // 지출 날짜 표현할거니까 문자열로 바꿔주고 카테고리는 null
                        (Double) result[1] // 지출 금액
                ))
                .collect(Collectors.toList());
    }

    /**
     * 라인차트
     * 사용자의 특정 가계부에 대한 날짜별 + 카테고리별 총 지출 금액 조회
     */
    public List<DateCategoryAmountDTO> getStatisticsByCategoryAndDates(Long accountBookId, Long memberId, List<Category> categories) {
        List<Object[]> results = expenseRepository.findTotalAmountByDatesAndCategories(accountBookId, memberId, categories);
        return results.stream()
                .map(result -> new DateCategoryAmountDTO(
                        ((LocalDateTime) result[0]).toString(),
                        (Category) result[1], // 전체 아니라서 카테고리 설정
                        (Double) result[2] // 지출 금액
                ))
                .collect(Collectors.toList());
    }






}
