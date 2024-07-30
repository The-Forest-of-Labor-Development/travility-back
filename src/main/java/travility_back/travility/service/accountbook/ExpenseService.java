package travility_back.travility.service.accountbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import travility_back.travility.dto.accountbook.ExpenseDTO;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Expense;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.ExpenseRepository;
import travility_back.travility.util.FileUploadUtil;

import java.io.IOException;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AccountBookRepository accountBookRepository;
    private final ObjectMapper objectMapper;

    //지출 등록
    @Transactional
    public ExpenseDTO createExpense(String expenseInfo, MultipartFile img) throws IOException {
        ExpenseDTO expenseDTO = null;
        try {
            expenseDTO = objectMapper.readValue(expenseInfo, ExpenseDTO.class); //역직렬화
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
           String newImgName = FileUploadUtil.uploadImage(img);
           expense.setImgName(newImgName);
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

        ExpenseDTO expenseDTO = null;
        try {
            expenseDTO = objectMapper.readValue(expenseInfo, ExpenseDTO.class); //역직렬화
        } catch (IOException e){
            throw new IllegalArgumentException("Invalid expenseInfo format", e);
        }

        //전달 받은 이미지가 있다면
        if(img != null && !img.isEmpty()){
            String newImgName = FileUploadUtil.uploadImage(img); //이미지 업로드

            //기존 이미지가 있다면
            if(expense.getImgName() != null && !expense.getImgName().isEmpty()){
                FileUploadUtil.deleteImage(expense.getImgName()); //기존 이미지 파일 삭제
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
        Expense expense = expenseRepository.findById(id).orElseThrow(()-> new NoSuchElementException("Expense not found"));
        //이미지가 있다면
        if(expense.getImgName() != null && !expense.getImgName().isEmpty()){
            FileUploadUtil.deleteImage(expense.getImgName()); //기존 이미지 파일 삭제
        }
        expenseRepository.deleteById(id);
    }
}