package travility_back.travility.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import travility_back.travility.config.UploadInform;
import travility_back.travility.dto.AccountBookDTO;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Budget;
import travility_back.travility.entity.Expense;
import travility_back.travility.entity.Member;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.MemberRepository;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountBookService {

    private final AccountBookRepository accountBookRepository;
    private final MemberRepository memberRepository;

    //가계부 전체 조회
//    @Transactional(readOnly=true)
//    public List<AccountBookDTO> getAllAccountBooks(String username) {
//        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new NoSuchElementException("Member not found with username: " + username));
//        return accountBookRepository.findByMember(member).stream()
//                .map(accountBook -> new AccountBookDTO(accountBook))
//                .collect(Collectors.toList());
//    }

    @Transactional(readOnly=true)
    public List<AccountBookDTO> getAllAccountBooks(String username, String sort) {
        //회원 찾기
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new NoSuchElementException("Member not found with username: " + username));

        //가계부 정렬
        List<AccountBook> accountBooks = accountBookRepository.findByMemberId(member.getId());;
        if (sort.equals("new")){ //최신순
            accountBooks = accountBookRepository.findByMemberOrderByStartDateDesc(member.getId());
        }else if (sort.equals("old")){ //오래된순
            accountBooks = accountBookRepository.findByMemberOrderByStartDateAsc(member.getId());
        }else if(sort.equals("highest")) { //높은 지출순
            accountBooks.sort((ab1,ab2)->Double.compare(calculateTotalExpenses(ab2),calculateTotalExpenses(ab1)));
        }else if(sort.equals("lowest")) { //낮은 지출순
            accountBooks.sort((ab1,ab2)->Double.compare(calculateTotalExpenses(ab1),calculateTotalExpenses(ab2)));
        }

        return accountBooks.stream()
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
            String path = UploadInform.uploadPath; //이미지 저장할 서버 주소
            String originalName = img.getOriginalFilename(); //파일 원본 이름
            String extension = originalName.substring(originalName.indexOf(".")); //파일 확장자
            String newImgName = UUID.randomUUID().toString() + extension; //새 이미지 이름
            img.transferTo(new File(path,newImgName)); //지정된 경로를 가진 새 파일 객체 생성하여 업로드

            System.out.println(originalName);
            //기존 이미지 파일 삭제
            if(accountBook.getImgName() != null && !accountBook.getImgName().isEmpty()){
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

    //예산의 통화 코드별 가중 평균 환율
    private Map<String, Double> calculateWeightedAverageExchangeRateByCurrency(List<Budget> budgets) {
        Map<String, Double> currencyToAvgExchangeRate = new HashMap<>(); //통화 코드별 가중 평균 환율
        Map<String, Double> currencyToTotalAmount = new HashMap<>(); //통화 코드별 총 예산 금액

        for(Budget budget : budgets) {
            String currency = budget.getCurUnit(); //통화 코드
            double amount = budget.getAmount(); //예산 금액
            double exchangeRate = budget.getExchangeRate().doubleValue(); //환율

            currencyToTotalAmount.put(currency, currencyToTotalAmount.getOrDefault(currency, 0.0) + amount); //통화 코드별 예산 총 금액
            currencyToAvgExchangeRate.put(currency, currencyToAvgExchangeRate.getOrDefault(currency, 0.0) + exchangeRate * amount); //통화 코드별 가중 합
        }

        for(String currency : currencyToAvgExchangeRate.keySet()) {
            double totalAmount = currencyToTotalAmount.get(currency); //해당 통화 코드의 총 예산 금액
            currencyToAvgExchangeRate.put(currency, currencyToAvgExchangeRate.get(currency) / totalAmount); //가중 합 비운 후, 가중 합 / 총 예산 금액 -> 가중 평균 환율
        }

        return currencyToAvgExchangeRate;
    }

    //가계부별 지출 총합
    private Double calculateTotalExpenses(AccountBook accountBook) {
        Map<String, Double> currencyToAvgExchangeRate = calculateWeightedAverageExchangeRateByCurrency(accountBook.getBudgets()); //통화 코드별 가중 평균 환율

        double totalExpenses = 0.0;
        for (Expense expense : accountBook.getExpenses()){
            String currency = expense.getCurUnit(); //통화 코드
            double amount = expense.getAmount(); //지출 금액
            double avgExchangeRate = currencyToAvgExchangeRate.get(currency); //해당 통화 코드 가중 평균 환율

            totalExpenses += amount * avgExchangeRate;
        }
        return totalExpenses;
    }
}