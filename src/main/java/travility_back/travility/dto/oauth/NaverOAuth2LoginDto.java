    package travility_back.travility.dto.oauth;

    import lombok.Data;
    import travility_back.travility.entity.enums.Role;

    @Data
    public class NaverOAuth2LoginDto {
        private String name;
        private String username;
        private Role role;
    }
