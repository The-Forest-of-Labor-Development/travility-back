package travility_back.travility.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import travility_back.travility.entity.enums.Category;

@Data
@AllArgsConstructor
public class CategoryAmountDTO {

    private Category category;
    private double amount;
}
