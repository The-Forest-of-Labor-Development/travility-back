package travility_back.travility.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import travility_back.travility.entity.Member;

import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final MemberDTO memberDTO;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return memberDTO.getRole();
            }
        });
        return collection;
    }

    @Override
    public String getPassword() {
        return memberDTO.getPassword();
    }

    @Override
    public String getUsername() {
        return memberDTO.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() { //사용자 계정 만료 여부
        return true;
    }

    @Override
    public boolean isAccountNonLocked() { //사용자 계정 잠금 여부
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() { //사용자 자격 증명 만료 여부
        return true;
    }

    @Override
    public boolean isEnabled() { //사용자 활성화 여부
        return true;
    }

    public MemberDTO getMemberDTO() {
        return memberDTO;
    }
}
