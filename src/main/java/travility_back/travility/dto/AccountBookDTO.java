package travility_back.travility.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class AccountBookDTO {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String countryName;
    private String countryFlag;
    private int numberOfPeople;
    private String title;
    private String imgName;
    private List<BudgetDTO> budgets;
    private MemberDTO member;
}