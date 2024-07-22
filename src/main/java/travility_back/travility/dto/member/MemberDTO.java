package travility_back.travility.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Role;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MemberDTO {
    private Long id;
    private String username;
    private String name;
    private String password;
    private String email;
    private String socialType;
    private LocalDateTime createdDate;
    private Role role;

    public MemberDTO(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.name = member.getName();
        this.password = member.getPassword();
        this.email = member.getEmail();
        this.socialType = member.getSocialType();
        this.createdDate = member.getCreatedDate();
        this.role = member.getRole();
    }
}
