package travility_back.travility.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String refresh;
    private String expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public RefreshToken(String refresh, String expiryDate, Member member) {
        this.refresh = refresh;
        this.expiryDate = expiryDate;
        this.member = member;
    }
}
