package travility_back.travility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import travility_back.travility.entity.Expense;
import travility_back.travility.entity.enums.Category;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * 지출 목록 조회
     */
    List<Expense> findByAccountBookId(Long accountbookId);

    /**
     * 일자별 통계(결제 방법)
     */
    @Query("select e from Expense e where e.accountBook.id = :accountBookId and e.expenseDate between :startOfDay and :endOfDay")
    List<Expense> findDailyAmountByPaymentMethod(@Param("accountBookId") Long accountBookId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 라인 차트(카테고리)
     */
    @Query("select e from Expense e where e.accountBook.id = :accountBookId and e.category = :category")
    List<Expense> findDailyAmountByCategoryForLineChart(@Param("accountBookId") Long accountBookId, @Param("category") Category category);

    /**
     * 날짜별 총 지출 조회
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.accountBook.id=:accountbookId and e.expenseDate BETWEEN :startDate AND :endDate")
    Double findTotalAmountByDateRange(@Param("accountbookId") Long id, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 통화 코드별 공동 경비 지출 목록 조회
     */
    @Query("select e from Expense e where e.accountBook.id = :accountBookId and e.curUnit = :curUnit and e.isShared = true")
    List<Expense> findSharedExpensesByAccountBookIdAndCurUnit(@Param("accountBookId")Long accountBookId, @Param("curUnit")String curUnit);

    /**
     * 공동 경비 지출 목록 조회
     */
    @Query("select e from Expense e where e.accountBook.id = :accountBookId and e.isShared = true")
    List<Expense> findSharedExpensesByAccountBookId(@Param("accountBookId") Long accountBookId);

    /**
     * 개인 경비 지출 목록 조회
     */
    @Query("select e from Expense e where e.accountBook.id = :accountBookId and e.isShared = false")
    List<Expense> findPersonalExpensesAccountBookId(@Param("accountBookId") Long accountBookId);

}