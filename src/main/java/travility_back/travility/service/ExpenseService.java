package travility_back.travility.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import travility_back.travility.dto.ExpenseDTO;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Expense;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.ExpenseRepository;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AccountBookRepository accountBookRepository;

    //지출 등록
    @Transactional
    public ExpenseDTO createExpense(String expenseInfo, MultipartFile img) throws IOException {
        //역직렬화
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); //타임스탬프 직렬화 disable
        System.out.println(expenseInfo);
        ExpenseDTO expenseDTO = null;
        try {
            expenseDTO = objectMapper.readValue(expenseInfo, ExpenseDTO.class);
        }catch (IOException e){
            throw new IllegalArgumentException("Invalid expenseInfo format", e);
        }

        //지출 등록할 가계부 찾기
        AccountBook accountBook = accountBookRepository.findById(expenseDTO.getAccountBookId())
                .orElseThrow(() -> new NoSuchElementException("AccountBook not found"));

        Expense expense = new Expense(expenseDTO, accountBook);

        //이미지 업로드
        if(img != null && !img.isEmpty()){
            //이미지 로컬 서버에 업로드
            String path = "C:/fullstack/final_project/images/"; //이미지 저장할 서버 주소
            String originalName = img.getOriginalFilename(); //파일 원본 이름
            String extension = originalName.substring(originalName.indexOf(".")); //파일 확장자
            String newImgName = UUID.randomUUID().toString() + extension; //새 이미지 이름
            img.transferTo(new File(path,newImgName)); //지정된 경로를 가진 새 파일 객체 생성하여 업로드

            expense.setImgName(newImgName);
        }else{
            expense.setImgName("default_image.png");
        }

        //지출 등록
        Expense savedExpense = expenseRepository.save(expense);

        expenseDTO.setId(savedExpense.getId());
        return expenseDTO;
    }

    //지출 수정
    @Transactional
    public void updateExpense(Long id, String expenseInfo, MultipartFile img) throws IOException {
        //수정할 지출 찾기
        Expense expense = expenseRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Expense not found"));

        //역직렬화
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); //날짜 및 시간 지원
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); //타임 스탬프 x

        ExpenseDTO expenseDTO = null;
        try {
            expenseDTO = objectMapper.readValue(expenseInfo, ExpenseDTO.class);
        } catch (IOException e){
            throw new IllegalArgumentException("Invalid expenseInfo format", e);
        }

        if(img != null && !img.isEmpty()){
            //이미지 로컬 서버에 업로드
            String path = "C:/fullstack/final_project/images/"; //이미지 저장할 서버 주소
            String originalName = img.getOriginalFilename(); //파일 원본 이름
            String extension = originalName.substring(originalName.indexOf(".")); //파일 확장자
            String newImgName = UUID.randomUUID().toString() + extension; //새 이미지 이름
            img.transferTo(new File(path,newImgName)); //지정된 경로를 가진 새 파일 객체 생성하여 업로드

            //기존 이미지 파일 삭제
            if(!originalName.equals("default_image.png") && expense.getImgName() != null && !expense.getImgName().isEmpty()){
                File oldImg = new File(path,expense.getImgName());
                if (oldImg.exists()){
                    oldImg.delete();
                }
            }
            expense.setImgName(newImgName);
        }

        //수정
        expense.setTitle(expenseDTO.getTitle());
        expense.setExpenseDate(expenseDTO.getExpenseDate());
        expense.setCategory(expenseDTO.getCategory());
        expense.setPaymentMethod(expenseDTO.getPaymentMethod());
        expense.setAmount(expenseDTO.getAmount());
        expense.setCurUnit(expenseDTO.getCurUnit());
        expense.setShared(expenseDTO.isShared());
        expense.setMemo(expenseDTO.getMemo());
    }

    //지출 삭제
    @Transactional
    public void deleteExpense(Long id){
        expenseRepository.deleteById(id);
    }
}