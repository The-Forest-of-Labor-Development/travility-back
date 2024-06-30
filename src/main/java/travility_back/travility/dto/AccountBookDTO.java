package travility_back.travility.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Member;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
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

    public AccountBookDTO(AccountBook accountBook){
        this.id = accountBook.getId();
        this.startDate = accountBook.getStartDate();
        this.endDate = accountBook.getEndDate();
        this.countryName = accountBook.getCountryName();
        this.countryFlag = accountBook.getCountryFlag();
        this.numberOfPeople = accountBook.getNumberOfPeople();
        this.title = accountBook.getTitle();
        this.imgName = accountBook.getImgName();
        this.budgets = accountBook.getBudgets().stream()
                .map(budget -> new BudgetDTO(budget))
                .collect(Collectors.toList());
    }
}