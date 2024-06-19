package travility_back.travility.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.entity.enums.Role;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Setter
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String username;
    
    // 소셜로그인 받을 때 실명 나옴
    private String name;

    private String password;

    private String email;

    private String birth;

    @Enumerated(EnumType.STRING)
    private Role role; // 권한 [USER, ADMIN]

    private Date createdDate; // 가입일자

    @OneToMany(mappedBy = "member")
    private List<AccountBook> accountBooks = new ArrayList<>();

}
