package travility_back.travility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import travility_back.travility.entity.Expense;
import travility_back.travility.entity.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * 마이리포트 페이지 노출
     */

    // 카테고리별 지출 금액 (개인 지출 + 공유 지출 / 인원수)
    @Query("select e.category, FLOOR(SUM(case when e.isShared = true then e.amount / ab.numberOfPeople else e.amount end)) " +
            "from Expense e JOIN e.accountBook ab WHERE ab.member.id = :memberId GROUP BY e.category")
    List<Object[]> findTotalAmountByCategory(@Param("memberId") Long memberId);

    // 결제 방법별 지출 금액 (개인 지출 + 공유 지출 / 인원수)
    @Query("select e.paymentMethod, FLOOR(SUM(case when e.isShared = true then e.amount / ab.numberOfPeople else e.amount end)) " +
            "from Expense e JOIN e.accountBook ab WHERE ab.member.id = :memberId GROUP BY e.paymentMethod")
    List<Object[]> findTotalAmountByPaymentMethod(@Param("memberId") Long memberId);

    List<Expense> findByAccountBookId(Long accountbookId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.accountBook.id=:accountbookId and e.expenseDate BETWEEN :startDate AND :endDate")
    Double findTotalAmountByDateRange(@Param("accountbookId") Long id, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);





//    // 카테고리별 지출 금액
//    @Query("select e.category, SUM(e.amount) from Expense e JOIN e.accountBook ab WHERE ab.member.id = :memberId GROUP BY e.category")
//    List<Object[]> findTotalAmountByCategory(@Param("memberId") Long memberId);
//
//    // 결제 방법별 지출 금액
//    @Query("select e.paymentMethod, SUM(e.amount) from Expense e JOIN e.accountBook ab WHERE ab.member.id = :memberId GROUP BY e.paymentMethod")
//    List<Object[]> findTotalAmountByPaymentMethod(@Param("memberId") Long memberId);




}

