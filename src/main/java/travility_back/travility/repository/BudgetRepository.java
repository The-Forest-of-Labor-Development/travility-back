package travility_back.travility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import travility_back.travility.entity.Budget;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    void deleteByAccountBookId(Long accountBookId);
    List<Budget> findByAccountBookId(Long accountBookId);
}