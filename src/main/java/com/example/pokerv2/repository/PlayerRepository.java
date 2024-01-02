package com.example.pokerv2.repository;

import com.example.pokerv2.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
