package com.library.repository;

import com.library.model.BorrowLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowLogRepository extends JpaRepository<BorrowLog, Long> {
    long countByStatus(String status);
    List<BorrowLog> findAllByOrderByBorrowDateDesc();
    List<BorrowLog> findByUserUsernameOrderByBorrowDateDesc(String username);
}
