package travility_back.travility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import travility_back.travility.dto.statistics.CategoryDateAmountDTO;
import travility_back.travility.entity.Expense;
import travility_back.travility.entity.enums.Category;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * 마이리포트
     */
    // 카테고리별 지출 금액 (개인 지출 + 공유 지출 / 인원수)
    @Query("select e.category, FLOOR(SUM(case when e.isShared = true then e.amount / ab.numberOfPeople else e.amount end)) " +
            "from Expense e JOIN e.accountBook ab WHERE ab.member.id = :memberId GROUP BY e.category")
    List<Object[]> findTotalAmountByCategory(@Param("memberId") Long memberId);

    // 결제 방법별 지출 금액 (개인 지출 + 공유 지출 / 인원수)
    @Query("select e.paymentMethod, FLOOR(SUM(case when e.isShared = true then e.amount / ab.numberOfPeople else e.amount end)) " +
            "from Expense e JOIN e.accountBook ab WHERE ab.member.id = :memberId GROUP BY e.paymentMethod")
    List<Object[]> findTotalAmountByPaymentMethod(@Param("memberId") Long memberId);



    /**
     * 지출통계
     */

    // 날짜별 카테고리 지출금액
    @Query("select e.expenseDate, e.category, SUM(case when e.isShared = true then floor((e.amount / ab.numberOfPeople)) else floor(e.amount) end) " +
            "from Expense e JOIN e.accountBook ab WHERE ab.id = :accountBookId AND ab.member.id = :memberId GROUP BY e.expenseDate, e.category")
    List<Object[]> findTotalAmountByDateAndCategory(@Param("accountBookId") Long accountBookId, @Param("memberId") Long memberId);

    // 날짜별 결제방법별 지출금액
    @Query("select e.paymentMethod, SUM(case when e.isShared = true then floor((e.amount / ab.numberOfPeople)) else floor(e.amount) end) " +
            "from Expense e JOIN e.accountBook ab WHERE ab.id = :accountBookId AND ab.member.id = :memberId AND e.expenseDate = :date GROUP BY e.paymentMethod")
    List<Object[]> findTotalAmountByPaymentMethodAndDate(@Param("accountBookId") Long accountBookId, @Param("memberId") Long memberId, @Param("date") LocalDate date);


    // 카테고리 총 지출
    @Query("select e.category, SUM(case when e.isShared = true then floor((e.amount / ab.numberOfPeople)) else floor(e.amount) end) " +
            "from Expense e JOIN e.accountBook ab WHERE ab.id = :accountBookId AND ab.member.id = :memberId GROUP BY e.category")
    List<Object[]> findTotalAmountByCategoryForAll(@Param("accountBookId") Long accountBookId, @Param("memberId") Long memberId);



    // 예산 - 지출
    @Query("SELECT SUM(CASE WHEN e.isShared = true THEN FLOOR(e.amount / ab.numberOfPeople) ELSE e.amount END) " +
            "FROM Expense e " +
            "JOIN e.accountBook ab " +
            "WHERE ab.id = :accountBookId")
    Double getTotalExpenseByAccountBookId(@Param("accountBookId") Long accountBookId);

    /**
     * 라인차트 사실 라디오 버튼용임
     */

    // 전체 지출 가져오기
    @Query("select e.expenseDate, SUM(case when e.isShared = true then e.amount / ab.numberOfPeople else e.amount end) " +
            "from Expense e JOIN e.accountBook ab WHERE ab.id = :accountBookId AND ab.member.id = :memberId GROUP BY e.expenseDate")
    List<Object[]> findTotalAmountByDates(@Param("accountBookId") Long accountBookId, @Param("memberId") Long memberId);

    // 특정 카테고리 지출 가져오기
    @Query("select e.expenseDate, e.category, SUM(case when e.isShared = true then e.amount / ab.numberOfPeople else e.amount end) " +
            "from Expense e JOIN e.accountBook ab WHERE ab.id = :accountBookId AND ab.member.id = :memberId AND e.category in :categories GROUP BY e.expenseDate, e.category")
    List<Object[]> findTotalAmountByDatesAndCategories(@Param("accountBookId") Long accountBookId, @Param("memberId") Long memberId, @Param("categories") List<Category> categories);




//    // 전체통계 : 카테고리별 전체 지출 작동 안할수도
//    @Query("select e.category, e.expenseDate, SUM(case when e.isShared = true then floor((e.amount / ab.numberOfPeople)) else floor(e.amount) end) " +
//            "from Expense e JOIN e.accountBook ab WHERE ab.id = :accountBookId AND ab.member.id = :memberId AND e.expenseDate BETWEEN :startDate AND :endDate GROUP BY e.category, e.expenseDate")
//    List<Object[]> findCategoryAmountByDate(@Param("accountBookId") Long accountBookId, @Param("memberId") Long memberId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}














//    // 한 일정에 대한 카테고리별 총 지출
//    @Query("select e.category, SUM(case when e.isShared = true then floor(e.amount / ab.numberOfPeople) else floor(e.amount) end) " +
//            "from Expense e JOIN e.accountBook ab WHERE ab.member.id = :memberId GROUP BY e.category")
//    List<Object[]> findTotalAmountByCategoryForAll(@Param("memberId") Long memberId);

    //    // 날짜별 카테고리 지출 금액 (개인 지출 + 공유 지출 / 인원수)
//    @Query("select e.expenseDate, e.category, SUM(case when e.isShared = true then e.amount / ab.numberOfPeople else e.amount end) " +
//            "from Expense e JOIN e.accountBook ab WHERE ab.id = :accountBookId AND ab.member.id = :memberId GROUP BY e.expenseDate, e.category")
//    List<Object[]> findTotalAmountByDateAndCategory(@Param("accountBookId") Long accountBookId, @Param("memberId") Long memberId); // 되는지 확인해야함(포매팅 삭제 -> 서비스로 옮김)
//
//    // 날짜별로 결제방법별 금액 가져오기
//    @Query("select e.paymentMethod, SUM(case when e.isShared = true then e.amount / ab.numberOfPeople else e.amount end) " +
//            "from Expense e JOIN e.accountBook ab WHERE ab.id = :accountBookId AND ab.member.id = :memberId AND e.expenseDate = :date GROUP BY e.paymentMethod")
//    List<Object[]> findTotalAmountByPaymentMethodAndDate(@Param("accountBookId") Long accountBookId, @Param("memberId") Long memberId, @Param("date") LocalDate date);
//


