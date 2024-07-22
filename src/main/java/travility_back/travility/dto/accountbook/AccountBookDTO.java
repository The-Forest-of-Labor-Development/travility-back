package travility_back.travility.dto.accountbook;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import travility_back.travility.dto.member.MemberDTO;
import travility_back.travility.entity.AccountBook;

import java.time.LocalDate;
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
    private List<ExpenseDTO> expenses;
    private MemberDTO member;

    public AccountBookDTO(AccountBook accountBook) {
        this.id = accountBook.getId();
        this.startDate = accountBook.getStartDate();
        this.endDate = accountBook.getEndDate();
        this.countryName = accountBook.getCountryName();
        this.countryFlag = accountBook.getCountryFlag();
        this.numberOfPeople = accountBook.getNumberOfPeople();
        this.title = accountBook.getTitle();
        this.imgName = accountBook.getImgName();
        this.budgets = accountBook.getBudgets().stream()
                .map(BudgetDTO::new)
                .collect(Collectors.toList());
        this.expenses = accountBook.getExpenses().stream()
                .map(ExpenseDTO::new)
                .collect(Collectors.toList());
        this.member = new MemberDTO(accountBook.getMember());
    }
}