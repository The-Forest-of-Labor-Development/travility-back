package travility_back.travility.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Getter
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
    private double totalBudget; //총 예산

    @Column(nullable = false)
    private String title; // 제목

    private String imgName;

    @OneToMany(mappedBy = "accountBook")
    private List<Budget> budgets;

    @OneToMany(mappedBy = "accountBook")
    private List<Expense> expenses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}
