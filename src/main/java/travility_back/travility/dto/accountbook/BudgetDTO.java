package travility_back.travility.dto.accountbook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import travility_back.travility.entity.Budget;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {
    private Long id;
    @JsonProperty("isShared")
    private boolean isShared;
    private String curUnit;
    private BigDecimal exchangeRate;
    private double amount;

    public BudgetDTO(Budget budget){
        this.id = budget.getId();
        this.isShared = budget.isShared();
        this.curUnit = budget.getCurUnit();
        this.exchangeRate = budget.getExchangeRate();
        this.amount = budget.getAmount();
    }
}
