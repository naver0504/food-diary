CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `pw` varchar(1000) DEFAULT NULL,
  `name` varchar(200) NOT NULL,
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
  `user_email` varchar(1000) DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;