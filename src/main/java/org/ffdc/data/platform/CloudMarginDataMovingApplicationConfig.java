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

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.storage.file.datalake.DataLakeServiceClient;
import com.azure.storage.file.datalake.DataLakeServiceClientBuilder;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableAutoConfiguration
@EnableScheduling
public class CloudMarginDataMovingApplicationConfig {
	@Value("${sas.token}")
	protected String sasToken = "";

	@Value("${storage.account.url}")
	protected String storageAccountUrl = "";

	@Value("${cosmos.key}")
	protected String cosmosKey= "";

	@Value("${cosmos.serviceEndpoint}")
	protected String serviceEndpoint = "";

	private static final Logger LOG = LoggerFactory.getLogger(CloudMarginDataMovingApplicationConfig.class);

	// @Bean
	// @ConfigurationProperties("app.datasource")
	// public DataSource dataLakeIntegrationDs()
	// {
	// 	return DataSourceBuilder.create().type(HikariDataSource.class).build();
	// }

	@Bean
	protected DataLakeServiceClient dataLakeFileSystemClient()
	{
		return new DataLakeServiceClientBuilder()
				.endpoint(storageAccountUrl + "?" + sasToken)
				.buildClient();
	}

	@Bean
	protected CosmosAsyncClient cosmosAsyncClient ()
	{
		return new CosmosClientBuilder()
							.endpoint(serviceEndpoint)
							.key(cosmosKey)
							.buildAsyncClient();
	}

	@Bean
    public ObjectMapper objectMapper() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        return objectMapper;
    }

}
