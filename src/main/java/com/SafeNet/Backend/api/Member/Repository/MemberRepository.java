package com.SafeNet.Backend.api.Member.Repository;

import com.SafeNet.Backend.api.Member.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member,String> {
}
