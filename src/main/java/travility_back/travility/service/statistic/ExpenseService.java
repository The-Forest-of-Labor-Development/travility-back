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

    public MyReportExpenseStatisticsDTO getStatistics(Long memberId) {
        MyReportExpenseStatisticsDTO myReportExpenseStatisticsDto = new MyReportExpenseStatisticsDTO();

        // 카테고리별 총액 조회
        List<Object[]> categoryAmounts = expenseRepository.findTotalAmountByCategory(memberId);
        String[] categories = new String[categoryAmounts.size()];
        double[] amounts = new double[categoryAmounts.size()];

        for (int i = 0; i < categoryAmounts.size(); i++) {
            categories[i] = ((Category) categoryAmounts.get(i)[0]).name();
            amounts[i] = (double) categoryAmounts.get(i)[1];
        }

        // 결제 방법별 총액 조회
        List<Object[]> paymentMethodAmounts = expenseRepository.findTotalAmountByPaymentMethod(memberId);
        PaymentMethodAmountDTO[] paymentMethods = new PaymentMethodAmountDTO[paymentMethodAmounts.size()];

        for (int i = 0; i < paymentMethodAmounts.size(); i++) {
            PaymentMethodAmountDTO paymentMethodAmountDTO = new PaymentMethodAmountDTO();
            paymentMethodAmountDTO.setPaymentMethod((PaymentMethod) paymentMethodAmounts.get(i)[0]);
            paymentMethodAmountDTO.setAmount((double) paymentMethodAmounts.get(i)[1]);
            paymentMethods[i] = paymentMethodAmountDTO;
        }

        // 전체 지출 총액 계산
        double totalAmount = 0;
        for (double amount : amounts) {
            totalAmount += amount;
        }

        myReportExpenseStatisticsDto.setCategories(categories);
        myReportExpenseStatisticsDto.setAmounts(amounts);
        myReportExpenseStatisticsDto.setPaymentMethods(paymentMethods);
        myReportExpenseStatisticsDto.setTotalAmount(totalAmount);

        return myReportExpenseStatisticsDto;
    }

    public MyReportExpenseStatisticsDTO getStatistics() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Long memberId = getMemberIdByUsername(username);

        return getStatistics(memberId);
    }

    private Long getMemberIdByUsername(String username) {
        Optional<Member> member = memberRepository.findByUsername(username);
        if (member.isPresent()) {
            return member.get().getId();
        } else {
            throw new UsernameNotFoundException(username);
        }
    }
}
