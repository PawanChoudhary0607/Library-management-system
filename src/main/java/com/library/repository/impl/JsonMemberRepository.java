package com.library.repository.impl;

import com.library.model.Member;
import com.library.repository.MemberRepository;
import com.library.util.JsonFileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JSON-file-backed implementation of {@link MemberRepository}. Follows
 * the same read-mutate-write pattern as {@code JsonBookRepository}; see
 * that class for the rationale.
 */
public class JsonMemberRepository implements MemberRepository {

    private static final Logger logger = LoggerFactory.getLogger(JsonMemberRepository.class);

    private final Path dataFile;
    private final JsonFileHandler jsonFileHandler;

    public JsonMemberRepository(Path dataFile, JsonFileHandler jsonFileHandler) {
        this.dataFile = Objects.requireNonNull(dataFile, "dataFile must not be null");
        this.jsonFileHandler = Objects.requireNonNull(jsonFileHandler, "jsonFileHandler must not be null");
    }

    @Override
    public Member save(Member member) {
        Objects.requireNonNull(member, "member must not be null");
        List<Member> members = loadAll();
        members.removeIf(existing -> existing.getId().equals(member.getId()));
        members.add(member);
        jsonFileHandler.writeList(dataFile, members);
        logger.info("Saved member: {}", member.getId());
        return member;
    }

    @Override
    public Optional<Member> findById(String id) {
        Objects.requireNonNull(id, "id must not be null");
        return loadAll().stream()
                .filter(member -> member.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        Objects.requireNonNull(email, "email must not be null");
        return loadAll().stream()
                .filter(member -> member.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public List<Member> findAll() {
        return loadAll();
    }

    @Override
    public List<Member> findByNameContaining(String keyword) {
        Objects.requireNonNull(keyword, "keyword must not be null");
        String lowerKeyword = keyword.toLowerCase();
        return loadAll().stream()
                .filter(member -> member.getName().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean deleteById(String id) {
        Objects.requireNonNull(id, "id must not be null");
        List<Member> members = loadAll();
        boolean removed = members.removeIf(member -> member.getId().equals(id));
        if (removed) {
            jsonFileHandler.writeList(dataFile, members);
            logger.info("Deleted member: {}", id);
        } else {
            logger.warn("Delete requested for unknown member id: {}", id);
        }
        return removed;
    }

    private List<Member> loadAll() {
        return new ArrayList<>(jsonFileHandler.readList(dataFile, Member.class));
    }
}
