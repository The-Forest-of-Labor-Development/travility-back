package travility_back.travility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import travility_back.travility.entity.Expense;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * 마이리포트 페이지 노출
     */

    // 카테고리별 지출 금액
    @Query("select e.category, SUM(e.amount) from Expense e JOIN e.accountBook ab WHERE ab.member.id = :memberId GROUP BY e.category")
    List<Object[]> findTotalAmountByCategory(@Param("memberId") Long memberId);

    // 결제 방법별 지출 금액
    @Query("select e.paymentMethod, SUM(e.amount) from Expense e JOIN e.accountBook ab WHERE ab.member.id = :memberId GROUP BY e.paymentMethod")
    List<Object[]> findTotalAmountByPaymentMethod(@Param("memberId") Long memberId);


}

