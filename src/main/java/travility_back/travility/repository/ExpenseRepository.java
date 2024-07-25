package travility_back.travility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import travility_back.travility.entity.Expense;
import travility_back.travility.entity.enums.Category;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByAccountBookId(Long accountbookId);

    /**
     * 마이리포트 페이지 노출
     */

    // 카테고리별 지출 금액 (개인 지출 + 공유 지출 / 인원수)
    // 카테고리별 지출 금액 (개인 지출 + 공유 지출 / 인원수)
    @Query("select e.category, FLOOR(SUM(case when e.isShared = true then (e.amount / ab.numberOfPeople) * b.exchangeRate else e.amount * b.exchangeRate end)) " +
            "from Expense e JOIN e.accountBook ab JOIN Budget b ON b.accountBook.id = ab.id and e.curUnit = b.curUnit " +
            "WHERE ab.member.id = :memberId GROUP BY e.category")
    List<Object[]> findTotalAmountByCategory(@Param("memberId") Long memberId);

    // 결제 방법별 지출 금액 (개인 지출 + 공유 지출 / 인원수)
    @Query("select e.paymentMethod, FLOOR(SUM(case when e.isShared = true then (e.amount / ab.numberOfPeople) * b.exchangeRate else e.amount * b.exchangeRate end)) " +
            "from Expense e JOIN e.accountBook ab JOIN Budget b ON b.accountBook.id = ab.id and e.curUnit = b.curUnit " +
            "WHERE ab.member.id = :memberId GROUP BY e.paymentMethod")
    List<Object[]> findTotalAmountByPaymentMethod(@Param("memberId") Long memberId);

    /**
     * 정산하기
     */

    @Query("select e from Expense e where e.accountBook.id = :accountBookId and e.curUnit = :curUnit and e.isShared = true")
    List<Expense> findSharedExpensesByAccountBookIdAndCurUnit(@Param("accountBookId")Long accountBookId, @Param("curUnit")String curUnit);

    /**
     * 캘린더
     */

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.accountBook.id=:accountbookId and e.expenseDate BETWEEN :startDate AND :endDate")
    Double findTotalAmountByDateRange(@Param("accountbookId") Long id, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


    /**
     * 지출통계
     */

    // 날짜별 카테고리 지출금액 조회
    @Query("select e.expenseDate, e.category, " +
            "SUM(FLOOR(e.amount * b.exchangeRate)) " +
            "from Expense e JOIN e.accountBook ab JOIN Budget b ON b.accountBook.id = ab.id and e.curUnit = b.curUnit " +
            "WHERE ab.id = :accountBookId AND ab.member.id = :memberId " +
            "GROUP BY e.expenseDate, e.category")
    List<Object[]> findTotalAmountByDateAndCategory(@Param("accountBookId") Long accountBookId, @Param("memberId") Long memberId);

    // 날짜별 결제방법별 지출금액 조회
    @Query("select e.paymentMethod, " +
            "SUM(FLOOR(e.amount * b.exchangeRate)), e.expenseDate " +
            "from Expense e JOIN e.accountBook ab JOIN Budget b ON b.accountBook.id = ab.id and e.curUnit = b.curUnit " +
            "WHERE ab.id = :accountBookId AND ab.member.id = :memberId AND e.expenseDate BETWEEN :startOfDay AND :endOfDay " +
            "GROUP BY e.paymentMethod, e.expenseDate")
    List<Object[]> findTotalAmountByPaymentMethodAndDate(@Param("accountBookId") Long accountBookId, @Param("memberId") Long memberId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // 카테고리별 총 지출금액 조회
    @Query("select e.category, " +
            "SUM(FLOOR(e.amount * b.exchangeRate)) " +
            "from Expense e JOIN e.accountBook ab JOIN Budget b ON b.accountBook.id = ab.id and e.curUnit = b.curUnit " +
            "WHERE ab.id = :accountBookId AND ab.member.id = :memberId " +
            "GROUP BY e.category")
    List<Object[]> findTotalAmountByCategoryForAll(@Param("accountBookId") Long accountBookId, @Param("memberId") Long memberId);

    /**
     * 지출통계 : 예산 - 지출
     * {@link BudgetRepository} 관련 코드 있음.
     */

    // 특정 가계부의 총 지출금액 조회
    @Query("SELECT SUM(FLOOR(e.amount * b.exchangeRate)) " +
            "FROM Expense e " +
            "JOIN e.accountBook ab JOIN Budget b ON b.accountBook.id = ab.id and e.curUnit = b.curUnit " +
            "WHERE ab.id = :accountBookId")
    Double getTotalExpenseByAccountBookId(@Param("accountBookId") Long accountBookId);

    /**
     * 라인차트 사실 라디오 버튼용임
     */

    // 날짜별 총 지출금액 조회
    @Query("select e.expenseDate, SUM(FLOOR(e.amount * b.exchangeRate)) " +
            "from Expense e JOIN e.accountBook ab JOIN Budget b ON b.accountBook.id = ab.id and e.curUnit = b.curUnit " +
            "WHERE ab.id = :accountBookId AND ab.member.id = :memberId GROUP BY e.expenseDate")
    List<Object[]> findTotalAmountByDates(@Param("accountBookId") Long accountBookId, @Param("memberId") Long memberId);


    // 특정 카테고리의 날짜별 지출금액 조회
    @Query("select e.expenseDate, e.category, SUM(FLOOR(e.amount * b.exchangeRate)) " +
            "from Expense e JOIN e.accountBook ab JOIN Budget b ON b.accountBook.id = ab.id and e.curUnit = b.curUnit " +
            "WHERE ab.id = :accountBookId AND ab.member.id = :memberId AND e.category in :categories GROUP BY e.expenseDate, e.category")
    List<Object[]> findTotalAmountByDatesAndCategories(@Param("accountBookId") Long accountBookId, @Param("memberId") Long memberId, @Param("categories") List<Category> categories);

    /**
     * 가계부 엑셀화
     */
    //공유 경비 지출
    @Query("select e from Expense e where e.accountBook.id = :accountBookId and e.isShared = true")
    List<Expense> findSharedExpensesByAccountBookId(@Param("accountBookId") Long accountBookId);

    //개인 경비 지출
    @Query("select e from Expense e where e.accountBook.id = :accountBookId and e.isShared = false")
    List<Expense> findPersonalExpensesAccountBookId(@Param("accountBookId") Long accountBookId);
}