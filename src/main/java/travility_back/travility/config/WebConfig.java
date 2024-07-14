package travility_back.travility.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") //모든 경로에 대한 CORS 설정 추가 (백엔드의 모든 엔드포인트)
                .allowedOrigins("http://localhost:3000") //이 URL에서 오는 요청 허용
                .exposedHeaders("Authorization","Set-Cookie")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**") //이 도메인으로 요청되는 리소스는
                .addResourceLocations(UploadInform.uploadLocation); //이 주소에 있는 리소스를 돌려줌 (file:/// : 파일 시스템의 최상위 디렉토리)
    }
}
