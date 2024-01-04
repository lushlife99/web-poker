package com.example.pokerv2.repository;

import com.example.pokerv2.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    Optional<Board> findByTotalPlayerBetween(int minTotalPlayer, int maxTotalPlayer);
}
