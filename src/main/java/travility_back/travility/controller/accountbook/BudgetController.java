package travility_back.travility.controller.accountbook;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import travility_back.travility.dto.accountbook.BudgetDTO;
import travility_back.travility.service.accountbook.BudgetService;

import java.util.List;

@RestController
@RequestMapping("/api/accountbook")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    //예산 수정
    @PutMapping("/{id}/budget")
    public List<BudgetDTO> updateBudget(@PathVariable Long id, @RequestBody List<BudgetDTO> budgets) {
        return budgetService.updateBudgets(id,budgets);
    }
}
