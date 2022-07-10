package com.angelo.dao;

import java.sql.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "player", schema = "public")
@Getter @Setter
public class Player {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private String nick;
	
	private short online;
	
	private short busy;
	
	private Date timestamp;
	
	private short italian;
	
	@ManyToMany(cascade = CascadeType.PERSIST)
	@JsonIgnore
	@JoinTable(name = "friendship", 
		joinColumns = {@JoinColumn(name = "player_1_id")}, 
		inverseJoinColumns = {@JoinColumn(name = "player_2_id")})
	private List<Player> following;	
	
	@ManyToMany(cascade = CascadeType.PERSIST)
	@JsonIgnore
	@JoinTable(name = "friendship", 
		joinColumns = {@JoinColumn(name = "player_2_id")}, 
		inverseJoinColumns = {@JoinColumn(name = "player_1_id")})
	private List<Player> followers;
	
}
