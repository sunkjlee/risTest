//stg 수정 : mod1, mod2git, mod3수정 
package config;  // 11월24일lkj - mod1, mod2, mod3수정

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import spring.ChangePasswordService;
import spring.MemberDao;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
@EnableTransactionManagement
public class AppConfig {

	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		ComboPooledDataSource ds = new ComboPooledDataSource();
		try {
			ds.setDriverClass("oracle.jdbc.driver.OracleDriver");
			//ds.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		}
		/*
		ds.setJdbcUrl("jdbc:mysql://localhost/spring4fs");
		ds.setUser("spring4");
		ds.setPassword("spring4");
		*/
		ds.setJdbcUrl("jdbc:oracle:thin:@l:1521:xe");
		ds.setUser("dbsec");
		ds.setPassword("dbsec");
		return ds;
	}

	@Bean
	public MemberDao memberDao() {
		return new MemberDao(dataSource());
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		DataSourceTransactionManager tm = new DataSourceTransactionManager();
		tm.setDataSource(dataSource());
		return tm;
	}

	@Bean
	public ChangePasswordService changePwdSvc() {
		return new ChangePasswordService(memberDao());
	}

}
