package org.ffdc.data.platform;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.ffdc.data.platform.Helpers.UrlParser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UrlParserTest {

    @Test
    public void getStorageNameFromValidUrl() throws URISyntaxException  {
        URI url = new URI("https://p01d15201500004.blob.core.windows.net/cloud-margin/sandbox/collateral-agreement-initial-margin-v1/2021-11-16T16:40:40.494Z.csv");
        UrlParser urlParser = new UrlParser();
        String storageName = urlParser.getStorageName(url);

        assertNotEquals("", storageName);
        assertEquals("p01d15201500004", storageName);
    }

    @Test
    public void getContainerNameFromValidUrl() throws URISyntaxException  {
        URI url = new URI("https://p01d15201500004.blob.core.windows.net/cloud-margin/sandbox/collateral-agreement-initial-margin-v1/2021-11-16T16:40:40.494Z.csv");
        UrlParser urlParser = new UrlParser();
        String containerName = urlParser.getContainerName(url);

        assertNotEquals("", containerName);
        assertEquals("cloud-margin", containerName);
    }

    @Test
    public void getTenantNameFromValidUrl() throws URISyntaxException  {
        URI url = new URI("https://p01d15201500004.blob.core.windows.net/cloud-margin/sandbox/collateral-agreement-initial-margin-v1/2021-11-16T16:40:40.494Z.csv");
        UrlParser urlParser = new UrlParser();
        String tenantName = urlParser.getTenantName(url);

        assertNotEquals("", tenantName);
        assertEquals("sandbox", tenantName);
    }

    @Test
    public void getFileExtensionFromValidUrl() throws URISyntaxException  {
        URI url = new URI("https://p01d15201500004.blob.core.windows.net/cloud-margin/sandbox/collateral-agreement-initial-margin-v1/2021-11-16T16:40:40.494Z.csv");
        UrlParser urlParser = new UrlParser();
        String extension = urlParser.getFileExtension(url);

        assertNotEquals("", extension);
        assertEquals("csv", extension);
    }

    @Test
    public void getFileNameFromValidUrl() throws URISyntaxException  {
        URI url = new URI("https://p01d15201500004.blob.core.windows.net/cloud-margin/sandbox/collateral-agreement-initial-margin-v1/2021-11-16T16:40:40.494Z.csv");
        UrlParser urlParser = new UrlParser();
        String extension = urlParser.getFileName(url);

        assertNotEquals("", extension);
        assertEquals("2021-11-16T16:40:40.494Z", extension);
    }
}

