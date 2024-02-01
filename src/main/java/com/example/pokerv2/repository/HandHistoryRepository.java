package com.example.pokerv2.repository;

import com.example.pokerv2.model.HandHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HandHistoryRepository extends JpaRepository<HandHistory, Long> {

    Optional<HandHistory> findByBoardIdAndGameSeq(Long boardId, Long gameSeq);
}
