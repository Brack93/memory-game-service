package com.angelo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.angelo.dao.Player;

public interface IPlayerDAO extends JpaRepository<Player, Long> {
	
	Optional<Player> findByNick(String nick);
	
}
