package travility_back.travility.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import travility_back.travility.entity.enums.Category;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CategoryDateAmountDTO {
    /**
     * 전체 지출 통계 : 라인그래프
     */
    private LocalDate date;
    private Category category;
    private Double amount;

    // 날짜형식 안받아오려고 생성자 추가했는데 맞는지 잘 모름
    public CategoryDateAmountDTO(LocalDate date, Category category, double amount) {
        this.date = date;
        this.category = category;
        this.amount = amount;
    }

    public CategoryDateAmountDTO(Category category, LocalDate date, Double amount) {
    }
}
