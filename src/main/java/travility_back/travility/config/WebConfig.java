package travility_back.travility.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") //모든 경로에 대한 CORS 설정 추가 (백엔드의 모든 엔드포인트)
                .allowedOrigins("http://localhost:3000") //이 URL에서 오는 요청 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
