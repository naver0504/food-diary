CREATE TABLE user (
    id int NOT NULL AUTO_INCREMENT,
    pw varchar(1000) DEFAULT NULL,
    role ENUM('admin', 'client') NULL DEFAULT 'client',
    status enum('active','delete','suspended') DEFAULT 'active',
    email varchar(1000) NOT NULL,
    create_path enum('google','kakao','none') NOT NULL DEFAULT 'none',
    create_at datetime DEFAULT CURRENT_TIMESTAMP,
    update_at datetime DEFAULT NULL,
    pw_try int DEFAULT NULL,
    pw_update_at datetime DEFAULT NULL,
    pw_update_delay_at datetime DEFAULT NULL,
    last_access_at datetime DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE session (
  token varchar(500) NOT NULL,
  create_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  terminate_at datetime NOT NULL,
  user_id int DEFAULT NULL,
  PRIMARY KEY (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `diary` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_at` datetime(6) NOT NULL,
  `diary_time` varchar(255) DEFAULT NULL,
  `geography` geometry DEFAULT NULL,
  `memo` varchar(500) DEFAULT NULL,
  `place` varchar(255) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `day` int NOT NULL,
  `month` int NOT NULL,
  `year` int NOT NULL,
  `update_at` datetime(6) DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf0xms46ulxc36096k9gg6j9ip` (`user_id`),
  CONSTRAINT `FKf0xms46ulxc36096k9gg6j9ip` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_at` datetime(6) NOT NULL,
  `stored_file_name` varchar(255) NOT NULL,
  `thumbnail_file_name` varchar(255) NOT NULL,
  `diary_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbdrqdeheur7qvwntqi42uhr2k` (`diary_id`),
  CONSTRAINT `FKbdrqdeheur7qvwntqi42uhr2k` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

 CREATE TABLE notice (
   id int NOT NULL AUTO_INCREMENT,
   title varchar(100) NOT NULL,
   content varchar(500) NOT NULL,
   available bit(1) NOT NULL DEFAULT b'0',
   notice_at date NOT NULL,
   create_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
   create_user_id int NOT NULL,
   update_at datetime,
   update_user_id int,
   PRIMARY KEY (id)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_at` datetime(6) NOT NULL,
  `tag_name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_1r1tyf6uga9k6jwdqnoqwtk2a` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `diary_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_at` datetime(6) NOT NULL,
  `update_at` datetime(6) DEFAULT NULL,
  `diary_id` int DEFAULT NULL,
  `tag_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfj71a53y08nd1aslcew09cf5w` (`diary_id`),
  KEY `FKoubxtedj8osbw8j4gksb3jy65` (`tag_id`),
  CONSTRAINT `FKfj71a53y08nd1aslcew09cf5w` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`),
  CONSTRAINT `FKoubxtedj8osbw8j4gksb3jy65` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

