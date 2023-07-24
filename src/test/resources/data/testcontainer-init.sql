CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `pw` varchar(1000) DEFAULT NULL,
  `name` varchar(200) NOT NULL,
  `status` enum('active','delete','suspended') DEFAULT 'active',
  `email` varchar(1000) NOT NULL,
  `create_path` enum('google','kakao','none') NOT NULL DEFAULT 'none',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `session` (
  `token` varchar(500) NOT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `terminate_at` datetime NOT NULL,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

create table image (
    id integer not null auto_increment,
    stored_file_name varchar(255) not null,
    time_status varchar(255),
    day_image_id integer,
    primary key (id)
) engine=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

create table day_image (
    day_image_id integer not null auto_increment,
    day integer not null,
    month integer not null,
    year integer not null,
    thumb_nail_image_path varchar(255),
    user_id integer,
    primary key (day_image_id)
) engine=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

