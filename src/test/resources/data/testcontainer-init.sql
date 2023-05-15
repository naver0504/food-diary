CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(200) NOT NULL,
  `pw` varchar(1000) DEFAULT NULL,
  `name` varchar(200) NOT NULL,
  `email` varchar(1000) NOT NULL,
  `create_path` enum('google','kakao','none') NOT NULL DEFAULT 'none',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;