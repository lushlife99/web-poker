package com.example.pokerv2.repository;

import com.example.pokerv2.model.HandHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HandHistoryRepository extends JpaRepository<HandHistory, Long> {
}
