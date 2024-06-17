package travility_back.travility.dto;

import lombok.Getter;

import java.util.Date;

@Getter
public class MemberDTO {
    private String username;
    private String password;
    private String email;
    private Date birth;
    private Date createdDate;
}
