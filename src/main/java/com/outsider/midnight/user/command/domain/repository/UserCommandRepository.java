package com.outsider.midnight.user.command.domain.repository;

import com.outsider.midnight.user.command.domain.aggregate.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserCommandRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    // 사용자 탈퇴 상태를 업데이트하는 커스텀 메서드
    @Modifying
    @Query("UPDATE User u SET u.isWithdrawal = true WHERE u = :user")
    void updateWithdrawalStatusByUser(@Param("user") User user);
}
