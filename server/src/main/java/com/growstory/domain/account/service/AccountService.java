package com.growstory.domain.account.service;

import com.growstory.domain.account.dto.AccountDto;
import com.growstory.domain.account.entity.Account;
import com.growstory.domain.account.repository.AccountRepository;
import com.growstory.domain.point.entity.Point;
import com.growstory.domain.point.service.PointService;
import com.growstory.global.auth.utils.AuthUserUtils;
import com.growstory.global.auth.utils.CustomAuthorityUtils;
import com.growstory.global.aws.service.S3Uploader;
import com.growstory.global.exception.BusinessLogicException;
import com.growstory.global.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class AccountService {
    private static final String ACCOUNT_IMAGE_PROCESS_TYPE = "profiles";

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthorityUtils authorityUtils;
    private final PointService pointService;
    private final S3Uploader s3Uploader;
    private final AuthUserUtils authUserUtils;

    public AccountDto.Response createAccount(AccountDto.Post accountPostDto, MultipartFile profileImage) {
        verifyExistsEmail(accountPostDto.getEmail());

        String encryptedPassword = passwordEncoder.encode(accountPostDto.getPassword());
        String profileImageUrl = s3Uploader.uploadImageToS3(profileImage, ACCOUNT_IMAGE_PROCESS_TYPE);
        List<String> roles = authorityUtils.createRoles(accountPostDto.getEmail());
        Point point = pointService.createPoint();

        Account savedAccount = accountRepository.save(Account.builder()
                .displayName(accountPostDto.getDisplayName())
                .email(accountPostDto.getEmail())
                .password(encryptedPassword)
                .point(point)
                .profileImageUrl(profileImageUrl)
                .roles(roles)
                .build());

        point.setAccount(savedAccount);

        return AccountDto.Response.builder()
                .accountId(savedAccount.getAccountId())
                .build();
    }

    public void updateProfileImage(MultipartFile profileImage) {
        Account findAccount = authUserUtils.getAuthUser();

        s3Uploader.deleteImageFromS3(findAccount.getProfileImageUrl(), ACCOUNT_IMAGE_PROCESS_TYPE);

        accountRepository.save(findAccount.toBuilder()
                .profileImageUrl(s3Uploader.uploadImageToS3(profileImage, ACCOUNT_IMAGE_PROCESS_TYPE))
                .build());
    }

    public void updateDisplayName(AccountDto.DisplayNamePatch displayNamePatchDto) {
        Account findAccount = authUserUtils.getAuthUser();

        accountRepository.save(findAccount.toBuilder()
                .displayName(displayNamePatchDto.getDisplayName())
                .build());
    }

    public void updatePassword(AccountDto.PasswordPatch passwordPatchDto) {
        Account findAccount = authUserUtils.getAuthUser();

        String encryptedChangedPassword = passwordEncoder.encode(passwordPatchDto.getChangedPassword());

        if (!passwordEncoder.matches(passwordPatchDto.getPresentPassword(), findAccount.getPassword())) throw new BadCredentialsException("현재 비밀번호가 일치하지 않습니다.");
        if (findAccount.getPassword().equals(encryptedChangedPassword)) throw new BadCredentialsException("새로운 비밀번호와 현재 비밀번호가 일치합니다.");

        accountRepository.save(findAccount.toBuilder()
                .password(encryptedChangedPassword)
                .build());
    }

    @Transactional(readOnly = true)
    public AccountDto.Response getAccount() {
        Account findAccount = authUserUtils.getAuthUser();

        return AccountDto.Response.builder()
                .accountId(findAccount.getAccountId())
                .displayName(authUserUtils.getAuthUser().getDisplayName())
                .profileImageUrl(findAccount.getProfileImageUrl())
                .point(findAccount.getPoint())
                .build();
    }

    public void deleteAccount() {
        Account findAccount = authUserUtils.getAuthUser();

        s3Uploader.deleteImageFromS3(findAccount.getProfileImageUrl(), ACCOUNT_IMAGE_PROCESS_TYPE);

        accountRepository.delete(findAccount);
    }

    private void verifyExistsEmail(String email) {
        Optional<Account> findAccount = accountRepository.findByEmail(email);

        if(findAccount.isPresent())
            throw new BusinessLogicException(ExceptionCode.ACCOUNT_ALREADY_EXISTS);
    }

    @Transactional(readOnly = true)
    public Account findVerifiedAccount() {
        Map<String, Object> principal = (Map<String, Object>) authUserUtils.getAuthUser();

        return accountRepository.findById((Long) principal.get("accountId")).orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.ACCOUNT_NOT_FOUND));
    }

    public void isAuthIdMatching(Long accountId) {
        Map<String, Object> claims = (Map<String, Object>) authUserUtils.getAuthUser();
        if ((Long) claims.get("accountId") != accountId)
            throw new BusinessLogicException(ExceptionCode.ACCOUNT_NOT_ALLOW);
    }

    public void buy(Account account, int price) {
        Point accountPoint = account.getPoint();
        int userPointScore = account.getPoint().getScore();
        if(price > userPointScore) {
            throw new BusinessLogicException(ExceptionCode.NOT_ENOUGH_POINTS);
        } else { // price <= this.point.getScore()
            int updatedScore = accountPoint.getScore()-price;
//            point.toBuilder().score(updatedScore).build(); //🔥 [refact] 더티체킹 여부 체크
//            account.toBuilder().point(point); //🔥 [refact] 필요?
            accountPoint.updateScore(updatedScore);
            account.updatePoint(accountPoint);
        }
    }

    public void resell(Account account, int price) {
        Point accountPoint = account.getPoint();
        int userPointScore = account.getPoint().getScore();

        int updatedScore = userPointScore + price;
        accountPoint.updateScore(updatedScore);
        account.updatePoint(accountPoint);
    }
}
