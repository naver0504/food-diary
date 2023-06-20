package com.fooddiary.api.common.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class Datasource {
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int maxPoolSize;
    @Value("${spring.datasource.hikari.minimum-idle}")
    private int minIdle;
    @Value("${spring.datasource.hikari.connection-timeout}")
    private int connectionTimeout;
    @Value("${spring.datasource.hikari.connection-test-sql}")
    private String connectionTestSql;

    @Bean
    public DataSource setDataSource(DataSourceProperties dataSourceProperties) throws SQLException {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setConnectionTimeout(connectionTimeout);
        hikariConfig.setMaximumPoolSize(maxPoolSize);
        hikariConfig.setMinimumIdle(minIdle);
        hikariConfig.setConnectionTestQuery(connectionTestSql);

        return new HikariDataSource(hikariConfig);
    }
}
