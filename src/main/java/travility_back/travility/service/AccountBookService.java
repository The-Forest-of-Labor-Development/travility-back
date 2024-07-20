package travility_back.travility.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import travility_back.travility.config.UploadInform;
import travility_back.travility.dto.AccountBookDTO;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Budget;
import travility_back.travility.entity.Expense;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Category;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.ExpenseRepository;
import travility_back.travility.repository.MemberRepository;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountBookService {

    private final AccountBookRepository accountBookRepository;
    private final MemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;

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

    //가계부 엑셀화 내보내기
    @Transactional
    public ResponseEntity<?> exportAccountBookToExcel(Long id, boolean krw) {
        try {
            // 가계부 찾기
            AccountBook accountBook = accountBookRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("AccountBook not found"));

            //지출 목록 오름차순 정렬
            List<Expense> expenses = accountBook.getExpenses();
            expenses.sort(Comparator.comparing(Expense::getExpenseDate));

            //카테고리별 지출 합계
            Map<String, Double> categoryToTotalAmount = initializeCategoryToTotalAmount();

            // 가계부의 가중 평균 환율
            Map<String, Double> currencyToAvgExchangeRate = calculateWeightedAverageExchangeRateByCurrency(accountBook.getBudgets());

            //try-with-resources
            // Workbook 생성
            try (SXSSFWorkbook workbook = new SXSSFWorkbook();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) { //바이트 배열기반 출력스트림. 메모리 내에서 파일 작성 -> 바이트 배열로 변환 -> HTTP 응답 본문

                Sheet sheet = workbook.createSheet("AccountBook");

                //셀 스타일
                //제목 행 스타일
                CellStyle titleStyle = workbook.createCellStyle();
                Font titleFont = workbook.createFont();
                titleFont.setBold(true); //글씨 굵게
                titleFont.setFontName("맑은 고딕");
                titleStyle.setFont(titleFont);
                titleFont.setFontHeightInPoints((short) 14);
                titleStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex()); //배경 하늘색
                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                titleStyle.setAlignment(HorizontalAlignment.CENTER);
                titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                //헤더 행 스타일
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); //배경 회색
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setAlignment(HorizontalAlignment.CENTER); // 가운데 정렬
                headerStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 수직 가운데 정렬


                //가계부 제목 행 생성
                Row titleRow = sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("가계부 정보");
                titleCell.setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 5)); //0~1행의 0~5열 병합

                // 가계부 정보 행 생성
                Row headerRow = sheet.createRow(2); //2행
                String[] headers = {"여행 제목", "여행 국가", "여행 시작 날짜", "여행 종료 날짜", "인원", "예산"};

                for (int i=0; i<headers.length; i++){
                    Cell headerCell = headerRow.createCell(i);
                    headerCell.setCellValue(headers[i]);
                    headerCell.setCellStyle(headerStyle);
                }

                //가계부 정보 내용 행 생성
                Row infoRow = sheet.createRow(3); //3행
                infoRow.createCell(0).setCellValue(accountBook.getTitle());
                infoRow.createCell(1).setCellValue(accountBook.getCountryName());
                infoRow.createCell(2).setCellValue(accountBook.getStartDate().toString());
                infoRow.createCell(3).setCellValue(accountBook.getEndDate().toString());
                infoRow.createCell(4).setCellValue(accountBook.getNumberOfPeople());

                double totalBugets = 0.0;
                for (Budget budget : accountBook.getBudgets()) {
                    totalBugets += budget.getExchangeRate().doubleValue() * budget.getAmount();
                }
                infoRow.createCell(5).setCellValue(Math.round(totalBugets)); //예산

                //지출 목록 제목 행 생성
                Row expensetitleRow = sheet.createRow(6);
                Cell expenseTitleCell = expensetitleRow.createCell(0);
                expenseTitleCell.setCellValue("지출 목록");
                expenseTitleCell.setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(6, 7, 0, 5)); //5행의 0~5열 병합

                // 지출 목록 헤더 행 생성
                Row expenseHeaderRow = sheet.createRow(8);
                String[] expenseHeaders = {"지출 일자", "지출 시간", "카테고리", "제목", "화폐", "금액"};

                for(int i=0; i<expenseHeaders.length; i++){
                    Cell expenseHeaderCell = expenseHeaderRow.createCell(i);
                    expenseHeaderCell.setCellValue(expenseHeaders[i]);
                    expenseHeaderCell.setCellStyle(headerStyle);
                }

                int rowNum = 9; //9행부터 시작
                for (Expense expense : expenses) {
                    Row expenseRow = sheet.createRow(rowNum++); //행 추가
                    expenseRow.createCell(0).setCellValue(expense.getExpenseDate().toLocalDate().toString()); //날짜+시간 -> 날짜 -> 문자열화
                    expenseRow.createCell(1).setCellValue(expense.getExpenseDate().toLocalTime().toString()); //날짜+시간 -> 시간 -> 문자열화
                    expenseRow.createCell(2).setCellValue(expense.getCategory().toString());
                    expenseRow.createCell(3).setCellValue(expense.getTitle());
                    if (krw) { //원화 계산
                        expenseRow.createCell(4).setCellValue("KRW");
                        int amountInKRW = (int) Math.round(currencyToAvgExchangeRate.get(expense.getCurUnit()) * expense.getAmount()); //소수점 없애고 반올림
                        expenseRow.createCell(5).setCellValue(amountInKRW);
                    } else { //그대로
                        expenseRow.createCell(4).setCellValue(expense.getCurUnit());
                        expenseRow.createCell(5).setCellValue(expense.getAmount());
                    }
                }

                //지출 통계
                //지출 통계 제목 행 생성
                rowNum = rowNum + 2;
                Row statisticstitleRow = sheet.createRow(rowNum);
                Cell statisticsHeaderCell = statisticstitleRow.createCell(0);
                statisticsHeaderCell.setCellValue("지출 통계");
                statisticsHeaderCell.setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum, ++rowNum, 0, 5)); //0~5열 병합


                //카테고리별 통계
                //카테고리 헤더 행 생성
                rowNum++;
                Row categoryHeaderRow = sheet.createRow(rowNum);
                String[] categoryHeaders = {"교통", "숙박", "식비", "관광", "쇼핑", "기타"};

                for(int i=0; i<categoryHeaders.length; i++){
                    Cell categoryHeaderCell = categoryHeaderRow.createCell(i);
                    categoryHeaderCell.setCellValue(categoryHeaders[i]);
                    categoryHeaderCell.setCellStyle(headerStyle);
                }

                //카테고리별 합계
                for (Expense expense : expenses){
                    String currency = expense.getCurUnit(); //통화 코드
                    String category = expense.getCategory().toString(); //카테고리
                    double amountInKRW = currencyToAvgExchangeRate.get(currency) * expense.getAmount(); //원화 지출
                    categoryToTotalAmount.put(category, categoryToTotalAmount.getOrDefault(category,0.0) + amountInKRW);
                }

                rowNum++;
                Row categoryRow = sheet.createRow(rowNum); //카테고리별 지출 행
                for (String category : categoryToTotalAmount.keySet()){
                    int totalAmount = (int)Math.round(categoryToTotalAmount.get(category));
                    if (category.equals("ACCOMMODATION")) {
                        categoryRow.createCell(0).setCellValue(totalAmount);
                    }else if (category.equals("TRANSPORTATION")){
                        categoryRow.createCell(1).setCellValue(totalAmount);
                    }else if (category.equals("FOOD")){
                        categoryRow.createCell(2).setCellValue(totalAmount);
                    } else if (category.equals("TOURISM")) {
                        categoryRow.createCell(3).setCellValue(totalAmount);
                    }else if (category.equals("SHOPPING")){
                        categoryRow.createCell(4).setCellValue(totalAmount);
                    }else if (category.equals("OTHERS")){
                        categoryRow.createCell(5).setCellValue(totalAmount);
                    }
                }

                //공유 경비 합계
                //공유 경비 헤더 행
                rowNum = rowNum + 2;
                Row totalSharedExpensesHeaderRow = sheet.createRow(rowNum);
                Cell totalSharedExpensesHeaderCell = totalSharedExpensesHeaderRow.createCell(0);
                totalSharedExpensesHeaderCell.setCellValue("공유 경비 합계");
                totalSharedExpensesHeaderCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5)); //0~5열 병합

                rowNum++;
                Row totalSharedExpensesRow = sheet.createRow(rowNum);
                List<Expense> sharedExpenses = expenseRepository.findSharedExpensesByAccountBookId(accountBook.getId());
                totalSharedExpensesRow.createCell(0).setCellValue(Math.round(calculateTotalExpensesByCurrency(sharedExpenses, currencyToAvgExchangeRate))); //공유 경비 합계
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5)); //0~5열 병합

                //개인 경비 합계
                //개인 경비 헤더 행
                rowNum = rowNum +2;
                Row totalPersonalExpensesHeaderRow = sheet.createRow(rowNum);
                Cell totalPersonalExpensesHeaderCell = totalPersonalExpensesHeaderRow.createCell(0);
                totalPersonalExpensesHeaderCell.setCellValue("개인 경비 합계");
                totalPersonalExpensesHeaderCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5)); //0~5열 병합

                rowNum++;
                Row totalPersonalExpensesRow = sheet.createRow(rowNum);
                List<Expense> personalExpenses = expenseRepository.findPersonalExpensesAccountBookId(accountBook.getId());
                totalPersonalExpensesRow.createCell(0).setCellValue(Math.round(calculateTotalExpensesByCurrency(personalExpenses, currencyToAvgExchangeRate))); //개인 경비 합계
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5)); //0~5열 병합

                //총 지출 합계
                //총 지출 헤더 행
                rowNum = rowNum +2;
                Row totalExpensesHeaderRow = sheet.createRow(rowNum);
                Cell totalExpensesHeaderCell = totalExpensesHeaderRow.createCell(0);
                totalExpensesHeaderCell.setCellValue("총 지출 합계");
                totalExpensesHeaderCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5)); //0~5열 병합

                rowNum++;
                Row totalExpensesRow = sheet.createRow(rowNum);
                totalExpensesRow.createCell(0).setCellValue(Math.round(calculateTotalExpenses(accountBook))); //총 지출
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5)); //0~5열 병합


                // 엑셀 파일 내용을 ByteArrayOutputStream에 저장 -> 메모리에 저장
                workbook.write(outputStream);
                byte[] bytes = outputStream.toByteArray(); //바이트 배열로 변환

                //파일이 저장된 bytes를 입력 스트림으로 읽기 -> 리소스 형태로 감싸기
                InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(bytes));

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=account_book.xlsx") //첨부파일, 다운로드 파일 이름
                        .contentType(MediaType.APPLICATION_OCTET_STREAM) //바이너리 데이터
                        .contentLength(bytes.length) //응답 본문을 파일 길이 만큼 지정
                        .body(inputStreamResource);

            }

        } catch (IOException e) {
            return new ResponseEntity<>("Fail export to Excel", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    //카테고리별 지출 초기화
    private Map<String, Double> initializeCategoryToTotalAmount() {
        Map<String, Double> categoryToTotalAmount = new HashMap<>();
        for (Category category : Category.values()){
            categoryToTotalAmount.put(category.toString(), 0.0);
        }
        return categoryToTotalAmount;
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

    //예산의 통화 코드별 (공동 or 개인) 경비 합계
    private Double calculateTotalExpensesByCurrency(List<Expense> expenses, Map<String, Double> currencyToAvgExchangeRate) {
        double totalExpenses = 0.0;
        for (Expense expense : expenses) {
            String currency = expense.getCurUnit(); //통화 코드
            double amount = expense.getAmount(); //지출 금액
            double avgExchangeRate = currencyToAvgExchangeRate.get(currency); //해당 통화 코드 가중 평균 환율

            totalExpenses += amount * avgExchangeRate;
        }
        return totalExpenses;
    }

}