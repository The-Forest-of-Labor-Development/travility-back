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
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new IllegalStateException("Member not found with username: " + username));
        return accountBookRepository.findByMember(member).stream()
                .map(accountBook -> new AccountBookDTO(accountBook))
                .collect(Collectors.toList());
    }

    //가계부 조회
    @Transactional(readOnly=true)
    public AccountBookDTO getAccountBookById(Long id) {
        AccountBook accountBook = accountBookRepository.findById(id).orElseThrow(() -> new IllegalStateException("AccountBook not found"));
        return new AccountBookDTO(accountBook);
    }

    //가계부 등록
    @Transactional
    public AccountBookDTO saveAccountBook(AccountBookDTO accountBookDTO, String username) {
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new IllegalStateException("Member not found with username: " + username));
        AccountBook accountBook = new AccountBook(accountBookDTO);
        accountBook.setMember(member);
        accountBook.setImgName("default_image.png");
        accountBookRepository.save(accountBook);
        return new AccountBookDTO(accountBook);
    }

    //가계부 삭제
    @Transactional
    public void deleteAccountBook(Long id) {
        accountBookRepository.deleteById(id);
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
        AccountBook accountBook = accountBookRepository.findById(id).orElseThrow(()-> new IllegalStateException("AccountBook not found"));

        //이미지 로컬 서버에 업로드
        String path = "C:/fullstack/final_project/images/"; //이미지 저장할 서버 주소
        String originalName = img.getOriginalFilename(); //파일 원본 이름
        String extension = originalName.substring(originalName.indexOf(".")); //파일 확장자
        String newImgName = UUID.randomUUID().toString() + extension; //새 이미지 이름
        img.transferTo(new File(path,newImgName)); //지정된 경로를 가진 새 파일 객체 생성하여 업로드

        //AccountBook 수정
        accountBook.setCountryName(accountBookDTO.getCountryName());
        accountBook.setCountryFlag(accountBookDTO.getCountryFlag());
        accountBook.setTitle(accountBookDTO.getTitle());
        accountBook.setStartDate(accountBookDTO.getStartDate());
        accountBook.setEndDate(accountBookDTO.getEndDate());
        accountBook.setImgName(newImgName);
    }

//    private AccountBookDTO convertToDTO(AccountBook accountBook) {
//        AccountBookDTO dto = new AccountBookDTO();
//        dto.setId(accountBook.getId());
//        dto.setStartDate(accountBook.getStartDate());
//        dto.setEndDate(accountBook.getEndDate());
//        dto.setCountryName(accountBook.getCountryName());
//        dto.setCountryFlag(accountBook.getCountryFlag());
//        dto.setImgName(accountBook.getImgName());
//        dto.setNumberOfPeople(accountBook.getNumberOfPeople());
//        dto.setTitle(accountBook.getTitle());
//        dto.setBudgets(accountBook.getBudgets().stream()
//                .map(Budget::toDTO)
//                .collect(Collectors.toList()));
//        dto.setMember(new MemberDTO(accountBook.getMember()));
//        return dto;
//    }
//
//    private AccountBook convertToEntity(AccountBookDTO accountBookDTO) {
//        AccountBook accountBook = new AccountBook();
//        accountBook.setId(accountBookDTO.getId());
//        accountBook.setStartDate(accountBookDTO.getStartDate());
//        accountBook.setEndDate(accountBookDTO.getEndDate());
//        accountBook.setCountryName(accountBookDTO.getCountryName());
//        accountBook.setCountryFlag(accountBookDTO.getCountryFlag());
//        accountBook.setNumberOfPeople(accountBookDTO.getNumberOfPeople());
//        accountBook.setTitle(accountBookDTO.getTitle());
//        accountBook.setImgName("default_image.png");
//        accountBook.setBudgets(accountBookDTO.getBudgets().stream()
//                .map(budgetDTO -> new Budget(budgetDTO, accountBook))
//                .collect(Collectors.toList()));
//
//        MemberDTO memberDTO = accountBookDTO.getMember();
//        if (memberDTO != null) {
//            Member member = memberRepository.findById(memberDTO.getMemberId())
//                    .orElseThrow(() -> new RuntimeException("User not found with id: " + memberDTO.getMemberId()));
//            accountBook.setMember(member);
//        }
//
//        return accountBook;
//    }
}