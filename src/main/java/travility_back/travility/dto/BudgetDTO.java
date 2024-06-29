package travility_back.travility.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import travility_back.travility.entity.Budget;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {
    private Long id;
    @JsonProperty("isShared")
    private boolean isShared;
    private String curUnit;
    private double exchangeRate;
    private double amount;

    public BudgetDTO(Budget budget){
        this.isShared = budget.isShared();
        this.curUnit = budget.getCurUnit();
        this.exchangeRate = budget.getExchangeRate();
        this.amount = budget.getAmount();
    }
}
