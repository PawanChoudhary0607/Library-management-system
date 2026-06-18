package com.library.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain entity representing a registered library member.
 *
 * <p>{@code id} and {@code membershipDate} are immutable once a member is
 * registered; contact details and {@link MemberStatus} can change over the
 * member's lifetime and are exposed through validated setters.
 */
public class Member {

    private final String id;
    private String name;
    private String email;
    private String phone;
    private final LocalDate membershipDate;
    private MemberStatus status;

    @JsonCreator
    public Member(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("phone") String phone,
            @JsonProperty("membershipDate") LocalDate membershipDate,
            @JsonProperty("status") MemberStatus status) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.phone = Objects.requireNonNull(phone, "phone must not be null");
        this.membershipDate = Objects.requireNonNull(membershipDate, "membershipDate must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name must not be null");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email, "email must not be null");
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = Objects.requireNonNull(phone, "phone must not be null");
    }

    public LocalDate getMembershipDate() {
        return membershipDate;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Member{id='" + id + "', name='" + name + "', email='" + email +
                "', phone='" + phone + "', membershipDate=" + membershipDate +
                ", status=" + status + '}';
    }
}
