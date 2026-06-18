package com.library.repository;

import com.library.model.Member;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for {@link Member} entities. See {@link BookRepository}
 * for the rationale behind the interface/implementation split.
 */
public interface MemberRepository {

    /**
     * Inserts a new member or overwrites the existing one with the same id.
     */
    Member save(Member member);

    Optional<Member> findById(String id);

    Optional<Member> findByEmail(String email);

    List<Member> findAll();

    /**
     * Case-insensitive substring search over member names.
     */
    List<Member> findByNameContaining(String keyword);

    boolean existsByEmail(String email);

    /**
     * @return true if a member with the given id was found and removed
     */
    boolean deleteById(String id);
}
