package travility_back.travility.service.accountbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import travility_back.travility.dto.accountbook.AccountBookDTO;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Budget;
import travility_back.travility.entity.Expense;
import travility_back.travility.entity.Member;
import travility_back.travility.entity.enums.Category;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.ExpenseRepository;
import travility_back.travility.repository.MemberRepository;
import travility_back.travility.util.CalcUtil;
import travility_back.travility.util.FileUploadUtil;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountBookService {

    private final AccountBookRepository accountBookRepository;
    private final MemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;
    private final ObjectMapper objectMapper;

    //가계부 전체 조회
    @Transactional(readOnly = true)
    public List<AccountBookDTO> getAllAccountBooks(String username, String sort) {
        //회원 찾기
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Member not found"));

        //가계부 정렬
        List<AccountBook> accountBooks = accountBookRepository.findByMemberId(member.getId());

        switch (sort) {
            case "new":
                accountBooks = accountBookRepository.findByMemberOrderByStartDateDesc(member.getId());
                break;
            case "old":
                accountBooks = accountBookRepository.findByMemberOrderByStartDateAsc(member.getId());
                break;
            case "highest":
                accountBooks.sort((ab1, ab2) -> Double.compare(CalcUtil.calculateTotalExpenses(ab2), CalcUtil.calculateTotalExpenses(ab1)));
                break;
            case "lowest":
                accountBooks.sort(Comparator.comparingDouble(CalcUtil::calculateTotalExpenses));
                break;
            default:
                break;

        }

        return accountBooks.stream()
                .map(accountBook -> new AccountBookDTO(accountBook))
                .collect(Collectors.toList());
    }

    //가계부 조회
    @Transactional(readOnly = true)
    public AccountBookDTO getAccountBookById(Long id) {
        AccountBook accountBook = accountBookRepository.findById(id).orElseThrow(() -> new NoSuchElementException("AccountBook not found"));
        return new AccountBookDTO(accountBook);
    }

    //가계부 등록
    @Transactional
    public AccountBookDTO createAccountBook(AccountBookDTO accountBookDTO, String username) {
        AccountBook accountBook = new AccountBook(accountBookDTO);
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Member not found with username: " + username));
        accountBook.setMember(member);
        accountBookRepository.save(accountBook);
        return new AccountBookDTO(accountBook);
    }

    //가계부 수정
    @Transactional
    public void updateAccountBook(Long id, String tripInfo, MultipartFile img) throws IOException {
        //tripInfo -> AccountBookDTO로 변환
        AccountBookDTO accountBookDTO = null;
        try {
            accountBookDTO = objectMapper.readValue(tripInfo, AccountBookDTO.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid tripInfo format");
        }

        //수정할 AccountBook 찾기
        AccountBook accountBook = accountBookRepository.findById(id).orElseThrow(() -> new NoSuchElementException("AccountBook not found"));

        if (img != null && !img.isEmpty()) { //전달받은 이미지가 있을 경우
            String newImgName = FileUploadUtil.uploadImage(img); //이미지 업로드

            //기존 이미지가 있을 경우
            if (accountBook.getImgName() != null && !accountBook.getImgName().isEmpty()) {
                FileUploadUtil.deleteImage(accountBook.getImgName()); //기존 이미지 삭제
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
        AccountBook accountBook = accountBookRepository.findById(id).orElseThrow(()-> new NoSuchElementException("AccountBook not found"));

        for (Expense expense : accountBook.getExpenses()){
            //지출 이미지가 있다면
            if(expense.getImgName() != null && !expense.getImgName().isEmpty()){
                FileUploadUtil.deleteImage(expense.getImgName()); //기존 이미지 파일 삭제
            }
        }

        //가계부 이미지가 있다면
        if (accountBook.getImgName() != null && !accountBook.getImgName().isEmpty()) {
            FileUploadUtil.deleteImage(accountBook.getImgName()); //기존 이미지 삭제
        }

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

            // 가계부의 가중 평균 환율
            Map<String, Double> currencyToAvgExchangeRate = CalcUtil.calculateWeightedAverageExchangeRateByCurrency(accountBook.getBudgets());

            //try-with-resources
            // Workbook 생성
            try (SXSSFWorkbook workbook = new SXSSFWorkbook();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) { //바이트 배열기반 출력스트림. 메모리 내에서 파일 작성 -> 바이트 배열로 변환 -> HTTP 응답 본문

                Sheet sheet = workbook.createSheet("AccountBook");

                //셀 스타일
                CellStyle titleStyle = createTitleCellStyle(workbook); //제목 행 스타일
                CellStyle headerStyle = createHeaderCellStyle(workbook); //헤더 행 스타일

                //엑셀 내용 작성
                int rowNum = 0;
                rowNum = writeAccountBookInfo(rowNum, sheet, titleStyle, headerStyle, accountBook);
                rowNum = writeExpenseList(rowNum, sheet, titleStyle, headerStyle, expenses, krw, currencyToAvgExchangeRate);
                writeExpenseStatistics(rowNum, sheet, titleStyle, headerStyle, expenses, currencyToAvgExchangeRate, accountBook);

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fail export to Excel");
        }
    }

    //셀 스타일 설정 (제목 행)
    private CellStyle createTitleCellStyle(Workbook workbook) {
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

        return titleStyle;
    }

    //셀 스타일 설정(헤더 행)
    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); //배경 회색
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER); // 가운데 정렬
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 수직 가운데 정렬

        return headerStyle;
    }

    //가계부 정보 엑셀 작성
    private int writeAccountBookInfo(int rowNum, Sheet sheet, CellStyle titleStyle, CellStyle headerStyle, AccountBook accountBook) {
        //가계부 정보 제목 행 생성
        Row titleRow = sheet.createRow(rowNum);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("가계부 정보");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, ++rowNum, 0, 5)); //0~1행의 0~5열 병합

        // 가계부 정보 서브 제목 행 생성
        Row headerRow = sheet.createRow(++rowNum); //2행
        String[] headers = {"여행 제목", "여행 국가", "여행 시작 날짜", "여행 종료 날짜", "인원", "예산"};

        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerStyle);
        }

        //가계부 정보 내용 행 생성
        Row infoRow = sheet.createRow(++rowNum); //3행
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

        return rowNum;
    }

    //지출 목록 엑셀 작성
    private int writeExpenseList(int rowNum, Sheet sheet, CellStyle titleStyle, CellStyle headerStyle, List<Expense> expenses, boolean krw, Map<String, Double> currencyToAvgExchangeRate) {
        //지출 목록 제목 행 생성
        rowNum += 3;
        Row expensetitleRow = sheet.createRow(rowNum);
        Cell expenseTitleCell = expensetitleRow.createCell(0);
        expenseTitleCell.setCellValue("지출 목록");
        expenseTitleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, ++rowNum, 0, 5)); //5행의 0~5열 병합

        // 지출 목록 서브 제목 행 생성
        Row expenseHeaderRow = sheet.createRow(++rowNum);
        String[] expenseHeaders = {"지출 일자", "지출 시간", "카테고리", "제목", "화폐", "금액"};

        for (int i = 0; i < expenseHeaders.length; i++) {
            Cell expenseHeaderCell = expenseHeaderRow.createCell(i);
            expenseHeaderCell.setCellValue(expenseHeaders[i]);
            expenseHeaderCell.setCellStyle(headerStyle);
        }

        rowNum++;
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

        return rowNum;
    }

    private void writeExpenseStatistics(int rowNum, Sheet sheet, CellStyle titleStyle, CellStyle headerStyle, List<Expense> expenses, Map<String, Double> currencyToAvgExchangeRate, AccountBook accountBook) {
        //지출 통계 제목 행 생성
        rowNum += 2;
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

        for (int i = 0; i < categoryHeaders.length; i++) {
            Cell categoryHeaderCell = categoryHeaderRow.createCell(i);
            categoryHeaderCell.setCellValue(categoryHeaders[i]);
            categoryHeaderCell.setCellStyle(headerStyle);
        }

        //카테고리별 합계
        Map<String, Double> categoryToTotalAmount = initializeCategoryToTotalAmount();

        for (Expense expense : expenses) {
            String currency = expense.getCurUnit(); //통화 코드
            String category = expense.getCategory().toString(); //카테고리
            double amountInKRW = currencyToAvgExchangeRate.get(currency) * expense.getAmount(); //원화 지출
            categoryToTotalAmount.put(category, categoryToTotalAmount.getOrDefault(category, 0.0) + amountInKRW);
        }

        rowNum++;
        Row categoryRow = sheet.createRow(rowNum); //카테고리별 지출 행
        for (String category : categoryToTotalAmount.keySet()) {
            int totalAmount = (int) Math.round(categoryToTotalAmount.get(category));
            if (category.equals("ACCOMMODATION")) {
                categoryRow.createCell(0).setCellValue(totalAmount);
            } else if (category.equals("TRANSPORTATION")) {
                categoryRow.createCell(1).setCellValue(totalAmount);
            } else if (category.equals("FOOD")) {
                categoryRow.createCell(2).setCellValue(totalAmount);
            } else if (category.equals("TOURISM")) {
                categoryRow.createCell(3).setCellValue(totalAmount);
            } else if (category.equals("SHOPPING")) {
                categoryRow.createCell(4).setCellValue(totalAmount);
            } else if (category.equals("OTHERS")) {
                categoryRow.createCell(5).setCellValue(totalAmount);
            }
        }

        //공유 경비 합계
        //공유 경비 헤더 행
        rowNum += 2;
        Row totalSharedExpensesHeaderRow = sheet.createRow(rowNum);
        Cell totalSharedExpensesHeaderCell = totalSharedExpensesHeaderRow.createCell(0);
        totalSharedExpensesHeaderCell.setCellValue("공유 경비 합계");
        totalSharedExpensesHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5)); //0~5열 병합

        rowNum++;
        Row totalSharedExpensesRow = sheet.createRow(rowNum);
        List<Expense> sharedExpenses = expenseRepository.findSharedExpensesByAccountBookId(accountBook.getId());
        totalSharedExpensesRow.createCell(0).setCellValue(Math.round(CalcUtil.calculateTotalExpensesByCurrency(sharedExpenses, currencyToAvgExchangeRate))); //공유 경비 합계
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5)); //0~5열 병합

        //개인 경비 합계
        //개인 경비 헤더 행
        rowNum += 2;
        Row totalPersonalExpensesHeaderRow = sheet.createRow(rowNum);
        Cell totalPersonalExpensesHeaderCell = totalPersonalExpensesHeaderRow.createCell(0);
        totalPersonalExpensesHeaderCell.setCellValue("개인 경비 합계");
        totalPersonalExpensesHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5)); //0~5열 병합

        rowNum++;
        Row totalPersonalExpensesRow = sheet.createRow(rowNum);
        List<Expense> personalExpenses = expenseRepository.findPersonalExpensesAccountBookId(accountBook.getId());
        totalPersonalExpensesRow.createCell(0).setCellValue(Math.round(CalcUtil.calculateTotalExpensesByCurrency(personalExpenses, currencyToAvgExchangeRate))); //개인 경비 합계
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5)); //0~5열 병합

        //총 지출 합계
        //총 지출 헤더 행
        rowNum += 2;
        Row totalExpensesHeaderRow = sheet.createRow(rowNum);
        Cell totalExpensesHeaderCell = totalExpensesHeaderRow.createCell(0);
        totalExpensesHeaderCell.setCellValue("총 지출 합계");
        totalExpensesHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5)); //0~5열 병합

        rowNum++;
        Row totalExpensesRow = sheet.createRow(rowNum);
        totalExpensesRow.createCell(0).setCellValue(Math.round(CalcUtil.calculateTotalExpenses(accountBook))); //총 지출
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5)); //0~5열 병합

    }

    //카테고리별 지출 초기화
    private Map<String, Double> initializeCategoryToTotalAmount() {
        Map<String, Double> categoryToTotalAmount = new HashMap<>();
        for (Category category : Category.values()) {
            categoryToTotalAmount.put(category.toString(), 0.0);
        }
        return categoryToTotalAmount;
    }



}