package travility_back.travility.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import travility_back.travility.entity.Member;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
public class MemberDTO {
    private String username;
    private String password;
    private String email;
    private String socialType;
    private LocalDateTime createdDate;
    private String role;

    public MemberDTO(Member member) {
        this.username = member.getUsername();
        this.password = member.getPassword();
        this.email = member.getEmail();
        this.socialType = member.getSocialType();
        this.createdDate = member.getCreatedDate();
        this.role = member.getRole().toString();
    }
}
