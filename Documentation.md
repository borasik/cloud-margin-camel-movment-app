# Documentation

## Url Pattern

Structure:
'http://blob-url/cloud-margin/ffdc-tenant/data-set-id/file-name.extention'

Sample:
'https://p01d15201500004.blob.core.windows.net/cloud-margin/sandbox/collateral-agreement-initial-margin-v1/2021-11-16T16:40:40.494Z.csv'

## API Url Application listening on

### Subscription / Move Data API

- localhost:8080/api/cloud-margin/v1/camel/pull-data

### Onboarding API [Add new tenant to mapping table]

- localhost:8080/XXX

## Usefully References

### Azure Storage Events

<https://docs.microsoft.com/en-us/azure/event-grid/receive-events>

### Camel Cosmos

<https://docs.microsoft.com/en-us/java/api/overview/azure/cosmos-readme?view=azure-java-stable>

### Camel SFTP

<https://turreta.com/2020/02/29/using-an-sftp-server-apache-camel-and-spring-boot/>

<http://www.masterspringboot.com/camel/camel-and-ftp-tutorial/>

### Java Cosmos SDK

<https://javadoc.io/static/com.azure/azure-cosmos/4.0.1-beta.1/index.html?overview-summary.html>

<https://github.com/Azure-Samples/azure-cosmos-java-sql-api-samples/blob/main/src/main/java/com/azure/cosmos/examples/documentcrud/sync/DocumentCRUDQuickstart.java#L162-L176>

<https://docs.microsoft.com/en-us/azure/cosmos-db/sql/sql-api-java-sdk-samples>

#### Important Notes

- Don't use '-' in name of tenants (in azure storage folder names)
