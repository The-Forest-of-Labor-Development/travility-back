package travility_back.travility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import travility_back.travility.dto.oauth.CustomOAuthUser;
import travility_back.travility.dto.oauth.NaverOAuth2LoginDto;
import travility_back.travility.dto.oauth.KakaoOAuth2LoginDto;
import travility_back.travility.dto.oauth.response.*;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Role;
import travility_back.travility.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.Optional;

// 사용자 정보 가져와서 db에 저장 / 갱신
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    /**
     * {@code CustomOAuth2UserService} OAuth로부터 사용자 정보 가져와서 db에 저장 및 갱신
     */

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // 어떤 OAuth2 인지
        OAuth2Response oAuth2Response = null;
        Member member = new Member();

        String accessToken = userRequest.getAccessToken().getTokenValue(); // OAuth2한테 받은 access token

        // 회원 소셜타입 정하기
        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes()); // 초기화
            member.setSocialType("naver");
        }
        else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes()); // 초기화
            member.setSocialType("google");
        }
        else if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes()); // 초기화
            member.setSocialType("kakao");
        }
        else {
            throw new IllegalArgumentException("Unknown registrationId: " + registrationId); // 등록id 못찾음
        }

        // 사용자명 생성 : 공급자(GOOGLE, NAVER, KAKAO) + 사용자 ID
        String username = oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();

        // 혹시 db에 사용자가 이미 있나?
        Optional<Member> isAlreadyLogin = memberRepository.findByUsername(username);

        // db에 사용자 없으면 새로 등록하기
        if (isAlreadyLogin.isEmpty()) {
            member.setUsername(username);
            member.setEmail(oAuth2Response.getEmail());
            member.setName(oAuth2Response.getName());
            member.setRole(Role.ROLE_USER);
            member.setCreatedDate(LocalDateTime.now());
            member.setAccessToken(accessToken);

            memberRepository.save(member);

            // OAuth2 종류에 맞게 dto생성
            if (registrationId.equals("naver")) {
                NaverOAuth2LoginDto naverDto = new NaverOAuth2LoginDto();
                naverDto.setUsername(username);
                naverDto.setName(oAuth2Response.getName());
                naverDto.setRole(Role.ROLE_USER);

                return new CustomOAuthUser(naverDto);
            }
            else if (registrationId.equals("kakao")) {
                KakaoOAuth2LoginDto kakaoDto = new KakaoOAuth2LoginDto();
                kakaoDto.setUsername(username);
                kakaoDto.setName(oAuth2Response.getName());
                kakaoDto.setRole(Role.ROLE_USER);

                return new CustomOAuthUser(kakaoDto);
            }
            else if (registrationId.equals("google")) {
                GoogleOAuth2LoginDto googleDto = new GoogleOAuth2LoginDto();
                googleDto.setUsername(username);
                googleDto.setName(oAuth2Response.getName());
                googleDto.setRole(Role.ROLE_USER);

                return new CustomOAuthUser(googleDto);
            }

        } else { // 혹시 사용자가 db에 있으면 갱신
            Member existData = isAlreadyLogin.get();
            existData.setEmail(oAuth2Response.getEmail());
            existData.setName(oAuth2Response.getName());
            existData.setAccessToken(accessToken);

            memberRepository.save(existData);

            // OAuth2 종류에 따라 dto 생성 후 반환
            if (registrationId.equals("naver")) {
                NaverOAuth2LoginDto naverDto = new NaverOAuth2LoginDto();
                naverDto.setUsername(username);
                naverDto.setName(oAuth2Response.getName());
                naverDto.setRole(existData.getRole());
                return new CustomOAuthUser(naverDto);
            } else if (registrationId.equals("kakao")) {
                KakaoOAuth2LoginDto kakaoDto = new KakaoOAuth2LoginDto();
                kakaoDto.setUsername(username);
                kakaoDto.setName(oAuth2Response.getName());
                kakaoDto.setRole(existData.getRole());
                return new CustomOAuthUser(kakaoDto);
            } else if (registrationId.equals("google")) {
                GoogleOAuth2LoginDto googleDto = new GoogleOAuth2LoginDto();
                googleDto.setUsername(username);
                googleDto.setName(oAuth2Response.getName());
                googleDto.setRole(existData.getRole());
                return new CustomOAuthUser(googleDto);
            }
        }
        return null;
    }
}
