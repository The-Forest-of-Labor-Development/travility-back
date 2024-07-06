package travility_back.travility.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import travility_back.travility.dto.AccountBookDTO;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Member;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.MemberRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountBookService {

    private final AccountBookRepository accountBookRepository;
    private final MemberRepository memberRepository;

    //가계부 전체 조회
    @Transactional(readOnly=true)
    public List<AccountBookDTO> getAllAccountBooks(String username) {
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new NoSuchElementException("Member not found with username: " + username));
        return accountBookRepository.findByMember(member).stream()
                .map(accountBook -> new AccountBookDTO(accountBook))
                .collect(Collectors.toList());
    }

    //가계부 조회
    @Transactional(readOnly=true)
    public AccountBookDTO getAccountBookById(Long id) {
        AccountBook accountBook = accountBookRepository.findById(id).orElseThrow(() -> new NoSuchElementException("AccountBook not found"));
        return new AccountBookDTO(accountBook);
    }

    //가계부 등록
    @Transactional
    public AccountBookDTO createAccountBook(AccountBookDTO accountBookDTO, String username) {
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new NoSuchElementException("Member not found with username: " + username));
        AccountBook accountBook = new AccountBook(accountBookDTO);
        accountBook.setMember(member);
        accountBook.setImgName("default_image.png");
        accountBookRepository.save(accountBook);
        return new AccountBookDTO(accountBook);
    }

    //가계부 수정
    @Transactional
    public void updateAccountBook(Long id, String tripInfo, MultipartFile img) throws IOException {
        //tripInfo -> AccountBookDTO로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); //타임스탬프 직렬화 disable

        AccountBookDTO accountBookDTO = null;
        try {
            accountBookDTO = objectMapper.readValue(tripInfo, AccountBookDTO.class);
        }catch (IOException e){
            throw new IllegalArgumentException("Invalid tripInfo format");
        }

        //수정할 AccountBook 찾기
        AccountBook accountBook = accountBookRepository.findById(id).orElseThrow(()-> new NoSuchElementException("AccountBook not found"));

        if(img!=null && !img.isEmpty()){ //이미지가 있을 경우
            //이미지 로컬 서버에 업로드
            String path = "C:/fullstack/final_project/images/"; //이미지 저장할 서버 주소
            String originalName = img.getOriginalFilename(); //파일 원본 이름
            String extension = originalName.substring(originalName.indexOf(".")); //파일 확장자
            String newImgName = UUID.randomUUID().toString() + extension; //새 이미지 이름
            img.transferTo(new File(path,newImgName)); //지정된 경로를 가진 새 파일 객체 생성하여 업로드

            //기존 이미지 파일 삭제
            if(!originalName.equals("default_image.png") && accountBook.getImgName() != null && !accountBook.getImgName().isEmpty()){
                File oldImg = new File(path,accountBook.getImgName());
                if (oldImg.exists()){
                    oldImg.delete();
                }
            }
            accountBook.setImgName(newImgName);
        }

        //AccountBook 수정
        accountBook.setCountryName(accountBookDTO.getCountryName());
        accountBook.setCountryFlag(accountBookDTO.getCountryFlag());
        accountBook.setTitle(accountBookDTO.getTitle());
        accountBook.setNumberOfPeople(accountBookDTO.getNumberOfPeople());
        accountBook.setStartDate(accountBookDTO.getStartDate());
        accountBook.setEndDate(accountBookDTO.getEndDate());
    }

    //가계부 삭제
    @Transactional
    public void deleteAccountBook(Long id) {
        accountBookRepository.deleteById(id);
    }
}