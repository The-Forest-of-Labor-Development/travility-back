package travility_back.travility.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travility_back.travility.dto.CustomUserDetails;
import travility_back.travility.entity.Member;
import travility_back.travility.repository.MemberRepository;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Transactional
    public ResponseEntity<?> forgotPassword(String username, String email) throws MessagingException {
        Optional<Member> optionalMember = memberRepository.findByUsername(username);

        //username을 가진 사용자가 없을 경우
        if (optionalMember.isEmpty()){
            return new ResponseEntity<>("Member not found", HttpStatus.BAD_REQUEST);
        }

        Member member = optionalMember.get();

        //소셜 로그인 사용자일 경우
        if (member.getSocialType() != null){
            return new ResponseEntity<>("Social login user", HttpStatus.BAD_REQUEST);
        }

        //DB에 저장된 이메일과 사용자가 입력한 이메일이 일치하지 않을 경우
        if(!(member.getEmail().equals(email))){
            return new ResponseEntity<>("Invalid email", HttpStatus.BAD_REQUEST);
        }

        //임시 비밀번호 생성
        String temporaryPassword = createTemporaryPassword();
        System.out.println(temporaryPassword);

        //임시 비밀번호 DB 저장
        member.setPassword(bCryptPasswordEncoder.encode(temporaryPassword));

        //이메일 전송
        try{
            sendTemporaryPasswordEmail(email, temporaryPassword);
        }catch (MessagingException e){
            e.printStackTrace();
            return new ResponseEntity<>("Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Success to send email", HttpStatus.OK);
    }

    //임시 비밀번호 생성
    private String createTemporaryPassword(){
        String lowercase = "abcdefghijklnmopqrstuvwxyz"; //영소문자
        String numbers = "0123456789"; //숫자
        String specialChars = "!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?"; //특수문자

        //임시 비밀번호 가능 조합
        String allowedChars = lowercase + numbers + specialChars;

        Random random = new Random();
        StringBuilder password = new StringBuilder();

        //15자리의 임시 비밀번호 생성
        for(int i=0; i<15; i++){
            int index = random.nextInt(allowedChars.length()); //allowedChars의 length까지의 숫자 제한. 난수 생성
            password.append(allowedChars.charAt(index));
        }

        return password.toString();
    }

    //임시 비밀번호 이메일 전송
    private void sendTemporaryPasswordEmail(String recipientEmail, String temporaryPassword) throws MessagingException {

        //이메일 객체 생성
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        //이메일 형식 html
        String htmlMsg = "<!DOCTYPE html>\n" +
                "<html lang=\"ko\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>임시 비밀번호 안내</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: 'Arial', sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;\">\n" +
                "    <div style=\"width: 100%; max-width: 600px; margin: 20px auto; padding: 20px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\">\n" +
                "        <div style=\"text-align: center; margin-bottom: 20px;\">\n" +
                "            <h2 style=\"color: #2A52BE; margin-bottom: 10px;\">임시 비밀번호 안내</h2>\n" +
                "            <p>임시 비밀번호입니다. 로그인 후에 비밀번호를 변경해 주세요.</p>\n" +
                "        </div>\n" +
                "        <div style=\"padding: 20px; border-radius: 8px; background-color: #f2f2f2; text-align: center;\">\n" +
                "            <strong style=\"font-size: 24px; font-weight: bold; color: #2A52BE;\">" + temporaryPassword + "</strong>\n" +
                "        </div>\n" +
                "        <p style=\"margin-top: 20px; text-align: center; font-size: 14px; color: #777777;\">이 메시지는 자동으로 발송되었습니다.</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        helper.setFrom(fromEmail);
        helper.setTo(recipientEmail); //수신자 이메일
        helper.setSubject("Travility 임시 비밀번호 발급 안내"); //이메일 제목
        helper.setText(htmlMsg, true); //html 변환 전달

        //메일 전송
        javaMailSender.send(mimeMessage);
    }

    //비밀번호 변경 전, 기존 비밀번호 확인
    @Transactional(readOnly = true)
    public boolean confirmPassword(CustomUserDetails userDetails, String password) {
        Member member = memberRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new NoSuchElementException("Member not found"));
        return bCryptPasswordEncoder.matches(password, member.getPassword()); //사용자가 입력한 비밀번호와 db에 저장된 비밀번호가 같은 지
    }

    //비밀번호 변경
    @Transactional
    public void updatePassword(CustomUserDetails userDetails, String password, HttpServletResponse response) throws IOException {
        Member member = memberRepository.findByUsername(userDetails.getUsername()).orElseThrow(()-> new NoSuchElementException("Member not found"));
        if (bCryptPasswordEncoder.matches(password, member.getPassword())){ //기존 비밀번호와 일치할 경우
            response.getWriter().write("Current password matches");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        member.setPassword(bCryptPasswordEncoder.encode(password)); //비밀번호 변경
    }
}
