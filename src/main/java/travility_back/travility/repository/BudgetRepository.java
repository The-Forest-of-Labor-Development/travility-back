package travility_back.travility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import travility_back.travility.entity.Budget;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    void deleteByAccountBookId(Long accountBookId);


    // 예산 - 지출
    @Query("SELECT SUM(CASE WHEN b.isShared = true THEN FLOOR(b.amount / ab.numberOfPeople) ELSE b.amount END) " +
            "FROM Budget b " +
            "JOIN b.accountBook ab " +
            "WHERE ab.id = :accountBookId")
    Double getTotalBudgetByAccountBookId(@Param("accountBookId") Long accountBookId);
}