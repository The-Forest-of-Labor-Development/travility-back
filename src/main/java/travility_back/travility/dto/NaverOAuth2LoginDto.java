package travility_back.travility.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import travility_back.travility.entity.enums.Role;

@Data
public class NaverOAuth2LoginDto {
    // 테스트임

    private String name;

    private String username;

    private Role role;
}
