package travility_back.travility.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import travility_back.travility.dto.AccountBookDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AccountBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_book_id")
    private Long id;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate; // 여행 시작일자

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate; // 여행 종료일자

    @Column(nullable = false)
    private String countryName;

    @Column(nullable = false)
    private String countryFlag;

    @Column(nullable = false)
    private int numberOfPeople; //인원

    @Column(nullable = false)
    private String title; // 제목

    private String imgName;

    @OneToMany(mappedBy = "accountBook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> budgets = new ArrayList<>();

    @OneToMany(mappedBy = "accountBook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public AccountBook(AccountBookDTO accountBookDTO){
        this.startDate = accountBookDTO.getStartDate();
        this.endDate = accountBookDTO.getEndDate();
        this.countryName = accountBookDTO.getCountryName();
        this.countryFlag = accountBookDTO.getCountryFlag();
        this.numberOfPeople = accountBookDTO.getNumberOfPeople();
        this.title = accountBookDTO.getTitle();
        this.budgets = accountBookDTO.getBudgets().stream()
                .map(budgetDTO -> new Budget(budgetDTO, this))
                .collect(Collectors.toList());
    }
}