package travility_back.travility.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travility_back.travility.dto.BudgetDTO;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Budget;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.BudgetRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private AccountBookRepository accountBookRepository;

    @Transactional
    public List<BudgetDTO> saveBudgets(Long accountBookId, List<BudgetDTO> budgetDTOs) {
        AccountBook accountBook = accountBookRepository.findById(accountBookId)
                .orElseThrow(() -> new RuntimeException("AccountBook not found"));

        List<Budget> budgets = budgetDTOs.stream()
                .map(dto -> new Budget(dto, accountBook))
                .collect(Collectors.toList());

        budgetRepository.deleteByAccountBookId(accountBookId); // 기존 예산 삭제
        budgetRepository.saveAll(budgets); // 새로운 예산 저장

        accountBook.getBudgets().clear();
        accountBook.getBudgets().addAll(budgets);
        accountBookRepository.save(accountBook);

        return budgets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private BudgetDTO convertToDTO(Budget budget) {
        return new BudgetDTO(
                budget.getId(),
                budget.isShared(),
                budget.getCurUnit(),
                budget.getExchangeRate(),
                budget.getAmount()
        );
    }
}