package travility_back.travility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import travility_back.travility.dto.CustomOAuthUser;
import travility_back.travility.dto.NaverOAuth2LoginDto;
import travility_back.travility.dto.OAuth2Response;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Role;
import travility_back.travility.repository.MemberRepository;
import travility_back.travility.response.GoogleResponse;
import travility_back.travility.response.NaverResponse;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;

        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes()); // 초기화
        }
        else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes()); // 초기화
        }
        else {
            return null;
        }

        // 리소스 서버에서 발급받은 정보로 아이디값 만들기
        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        // 해당 유저가 이미 로그인 했는지
        Member isAlreadyLogin = memberRepository.findByUsername(username);

        // 한번도 로그인하지 않아서 null인경우
        if (isAlreadyLogin == null) {

            Member member = new Member();
            member.setUsername(username);
            member.setEmail(oAuth2Response.getEmail());
            member.setName(oAuth2Response.getName());
            member.setRole(Role.USER);

            memberRepository.save(member);

            // dto에 저장
            NaverOAuth2LoginDto naverDto = new NaverOAuth2LoginDto();
            naverDto.setUsername(username);
            naverDto.setName(oAuth2Response.getName());
            naverDto.setRole(Role.USER);

            return new CustomOAuthUser(naverDto);

        }
        // 한번이라도 로그인을 진행해서 데이터가 존재하는경우
        else {
            // 데이터를 업데이트해줘야함
            isAlreadyLogin.setEmail(oAuth2Response.getEmail());
            isAlreadyLogin.setName(oAuth2Response.getName());

            memberRepository.save(isAlreadyLogin);

            // dto에 저장
            NaverOAuth2LoginDto naverDto = new NaverOAuth2LoginDto();
            naverDto.setUsername(isAlreadyLogin.getUsername());
            // name은 바뀐걸로 갖고와야해서 oAuth2Response에서 갖고옴
            naverDto.setName(oAuth2Response.getName());
            naverDto.setRole(isAlreadyLogin.getRole());

            return new CustomOAuthUser(naverDto);

        }
    }
}
