package travility_back.travility.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import travility_back.travility.entity.enums.Category;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateCategoryAmountDTO {

    private String date;
    private Category category;
    private Double amount;
}
