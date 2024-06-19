package travility_back.travility.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
public class AccountBook {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "account_book_id")
    private Long id;

    private Date startDate; // 여행 시작일자

    private Date endDate; // 여행 종료일자

//    private Country country; // 국가정보 (api)

    private int numberOfPeople;

    private String imgName;

    private String comment; // 후기

    @OneToMany
    private List<Expense> expenses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}
