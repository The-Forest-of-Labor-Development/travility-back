package travility_back.travility.dto;

public interface OAuth2Response {

    // provider (naver, google, kakao, ...)
    String getProvider();

    // ID
    String getProviderId();

    // email
    String getEmail();

    // username
    String getName();
}

