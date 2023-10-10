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

create table diary (
    id int NOT NULL AUTO_INCREMENT,
    day int NOT NULL,
    month int NOT NULL,
    year int NOT NULL,
    memo varchar(255) DEFAULT NULL,
    create_time datetime DEFAULT NULL,
    diary_time enum('breakfast','brunch','lunch','snack','linner','dinner','latesnack','etc') DEFAULT NULL,
    create_at datetime DEFAULT NULL,
    update_at datetime DEFAULT NULL,
    user_id int DEFAULT NULL,
    primary key (id)
) engine=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

create table image (
    id int NOT NULL AUTO_INCREMENT,
    stored_file_name varchar(255) NOT NULL,
    time_status varchar(255) DEFAULT NULL,
    diary_id int DEFAULT NULL,
    geography geometry DEFAULT NULL,
    create_at datetime DEFAULT NULL,
    diary_time varchar(255) DEFAULT NULL,
    thumbnail_file_name varchar(255) NOT NULL,
    user_id int DEFAULT NULL,
    primary key (id)
) engine=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

create table day_image (
    day_image_id integer not null auto_increment,
    day integer not null,
    month integer not null,
    year integer not null,
    create_time datetime,
    geography GEOMETRY,
    thumb_nail_image_path varchar(255),
    user_id integer,
    primary key (day_image_id)
) engine=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


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

