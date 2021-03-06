package com.example.config;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.mysql.jdbc.Connection;

/**
 * mysql connection config
 * Created by bloodkilory on 15/5/10.
 */
@Configuration
@MapperScan(basePackages = "com.example.mapper")
public class LocalMysqlConfig {
    @Bean
	public DataSource dataSource() {
		PooledDataSource dataSource = new PooledDataSource();
		dataSource.setDriver("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/mysqlDB");
		dataSource.setUsername("root");
		dataSource.setPassword("toor");
		dataSource.setDefaultTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
		dataSource.setPoolMaximumActiveConnections(10);
		return dataSource;
	}

	/**
	 * 可同时支持spring-data-redis和mybatis使用
	 * {@link @Transactional}
	 *
	 * @return
	 */
	@Bean
	public PlatformTransactionManager platformTransactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		sessionFactory.setDataSource(dataSource());
		return sessionFactory.getObject();
	}
}
