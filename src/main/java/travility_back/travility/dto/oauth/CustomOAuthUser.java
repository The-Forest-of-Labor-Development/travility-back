package travility_back.travility.dto.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuthUser implements OAuth2User {

    private final NaverOAuth2LoginDto naverOAuth2LoginDto;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return naverOAuth2LoginDto.getRole().name();
            }
        });

        return collection;
    }

    @Override
    public String getName() {
        return naverOAuth2LoginDto.getName();
    }

    // username 호출
    public String getUsername() {
        return naverOAuth2LoginDto.getUsername();
    }


}
