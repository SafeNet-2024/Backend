package com.SafeNet.Backend.domain.member.repository;

import com.SafeNet.Backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,String> {
    Optional<Member> findByEmail(String email);//Optional<Member> NPE 발생을 방지

    boolean existsMemberByName(String name);
}
