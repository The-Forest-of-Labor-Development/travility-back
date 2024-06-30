package travility_back.travility.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import travility_back.travility.dto.AccountBookDTO;
import travility_back.travility.dto.MemberDTO;
import travility_back.travility.entity.AccountBook;
import travility_back.travility.entity.Budget;
import travility_back.travility.entity.Member;
import travility_back.travility.repository.AccountBookRepository;
import travility_back.travility.repository.MemberRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountBookService {

    private final AccountBookRepository accountBookRepository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    public List<AccountBookDTO> getAllAccountBooks() {
        return accountBookRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<AccountBookDTO> getAccountBookById(Long id) {
        return accountBookRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public AccountBookDTO saveAccountBook(AccountBookDTO accountBookDTO) {
        AccountBook accountBook = convertToEntity(accountBookDTO);
        accountBook = accountBookRepository.save(accountBook);
        return convertToDTO(accountBook);
    }

    public void deleteAccountBook(Long id) {
        accountBookRepository.deleteById(id);
    }

    private AccountBookDTO convertToDTO(AccountBook accountBook) {
        AccountBookDTO dto = new AccountBookDTO();
        dto.setId(accountBook.getId());
        dto.setStartDate(accountBook.getStartDate());
        dto.setEndDate(accountBook.getEndDate());
        dto.setCountryName(accountBook.getCountryName());
        dto.setCountryFlag(accountBook.getCountryFlag());
        dto.setNumberOfPeople(accountBook.getNumberOfPeople());
        dto.setTitle(accountBook.getTitle());
        dto.setBudgets(accountBook.getBudgets().stream()
                .map(Budget::toDTO)
                .collect(Collectors.toList()));
        dto.setMember(new MemberDTO(accountBook.getMember()));
        return dto;
    }

    private AccountBook convertToEntity(AccountBookDTO accountBookDTO) {
        AccountBook accountBook = new AccountBook();
        accountBook.setId(accountBookDTO.getId());
        accountBook.setStartDate(accountBookDTO.getStartDate());
        accountBook.setEndDate(accountBookDTO.getEndDate());
        accountBook.setCountryName(accountBookDTO.getCountryName());
        accountBook.setCountryFlag(accountBookDTO.getCountryFlag());
        accountBook.setNumberOfPeople(accountBookDTO.getNumberOfPeople());
        accountBook.setTitle(accountBookDTO.getTitle());
        accountBook.setBudgets(accountBookDTO.getBudgets().stream()
                .map(budgetDTO -> new Budget(budgetDTO, accountBook))
                .collect(Collectors.toList()));

        MemberDTO memberDTO = accountBookDTO.getMember();
        if (memberDTO != null) {
            Member member = memberRepository.findById(memberDTO.getId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + memberDTO.getId()));
            accountBook.setMember(member);
        }

        return accountBook;
    }
}