CREATE TABLE player (
    id bigint NOT NULL auto_increment,
    nick varchar(255) NOT NULL,
    `online` smallint,
    busy smallint,
    subscription varchar(255),
    `timestamp` date,
    contact varchar(255),
    italian smallint,
    primary key (id)
);

CREATE TABLE friendship (
    id bigint NOT NULL auto_increment,
    player_1_id bigint NOT NULL,
    player_2_id bigint NOT NULL,
    primary key (id),
    CONSTRAINT player_1_id foreign key (player_1_id) references player(id),
	CONSTRAINT player_2_id foreign key (player_2_id) references player(id)
);