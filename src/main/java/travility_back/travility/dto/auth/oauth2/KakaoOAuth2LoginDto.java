package travility_back.travility.dto.auth.oauth2;

import lombok.Data;
import travility_back.travility.entity.enums.Role;

@Data
public class KakaoOAuth2LoginDto {
    private String name;
    private String username;
    private Role role;
}
