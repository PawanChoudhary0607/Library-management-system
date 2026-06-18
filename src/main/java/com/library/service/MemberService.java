package com.library.service;

import com.library.exception.ValidationException;
import com.library.model.Member;
import com.library.model.MemberStatus;
import com.library.repository.MemberRepository;
import com.library.util.DateUtil;
import com.library.util.IdGenerator;
import com.library.util.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Business operations for managing library members.
 *
 * <p>Depends only on {@link MemberRepository} — no other service, and no
 * file or JSON handling of any kind.
 */
public class MemberService {

    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
    }

    /**
     * Registers a new member with {@link MemberStatus#ACTIVE} status and
     * today's date as the membership date.
     *
     * @throws ValidationException if the email is already registered
     */
    public Member registerMember(String name, String email, String phone) {
        InputValidator.requireNonBlank(name, "name");
        InputValidator.requireEmail(email);
        InputValidator.requirePhone(phone);

        if (memberRepository.existsByEmail(email)) {
            logger.warn("Attempted to register duplicate email: {}", email);
            throw new ValidationException("A member with email " + email + " is already registered");
        }

        Member member = new Member(IdGenerator.generate("MEM"), name, email, phone,
                DateUtil.today(), MemberStatus.ACTIVE);
        memberRepository.save(member);
        logger.info("Registered member '{}' (id={})", name, member.getId());
        return member;
    }

    /**
     * Case-insensitive search across name and email.
     */
    public List<Member> searchMembers(String keyword) {
        InputValidator.requireNonBlank(keyword, "keyword");
        String lowerKeyword = keyword.toLowerCase();
        return memberRepository.findAll().stream()
                .filter(member -> member.getName().toLowerCase().contains(lowerKeyword)
                        || member.getEmail().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }
}
