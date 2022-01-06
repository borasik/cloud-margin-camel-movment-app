package org.ffdc.data.platform;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.azure.storage.file.datalake.DataLakeServiceClient;
import com.azure.storage.file.datalake.DataLakeServiceClientBuilder;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableAutoConfiguration
@EnableScheduling
public class CloudMarginDataMovingApplicationConfig {
	@Value("${sas.token}")
	protected String sasToken = "";
	@Value("${storage.account.url}")
	protected String storageAccountUrl = "";
	
	private static final Logger LOG = LoggerFactory.getLogger(CloudMarginDataMovingApplicationConfig.class);
	
	@Bean
	@ConfigurationProperties("app.datasource")
	public DataSource dataLakeIntegrationDs() {
		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}
	
	@Bean
	protected DataLakeServiceClient dataLakeFileSystemClient() {	
		System.out.println("test"+storageAccountUrl + "?" + sasToken);
		return new DataLakeServiceClientBuilder()
				.endpoint(storageAccountUrl + "?" + sasToken)														
				.buildClient();
	}

}
