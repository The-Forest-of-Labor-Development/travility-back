package travility_back.travility.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.entity.enums.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    // 소셜로그인 받을 때 실명 나옴
    private String name;

    private String password;

    private String email;

    private String socialType;

    @Enumerated(EnumType.STRING)
    private Role role; // 권한 [ROLE_USER, ROLE_ADMIN]

    private LocalDateTime createdDate; // 가입일자

    private String oauth2Token;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>(); //다중 디바이스 고려

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountBook> accountBooks = new ArrayList<>();

    public Member(MemberDTO memberDTO){
        this.username = memberDTO.getUsername();
        this.name = memberDTO.getName();
        this.password = memberDTO.getPassword();
        this.email = memberDTO.getEmail();
        this.role = memberDTO.getRole();
        this.createdDate = memberDTO.getCreatedDate();
    }
}
