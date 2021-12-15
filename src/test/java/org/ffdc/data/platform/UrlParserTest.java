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
        URI url = new URI("https://p01d15201500002.blob.core.windows.net/alex2/trades-v1-27a8371b-9317-43ed-82c0-39835cf1ec03/2020-05-15T06:23:56-01:00.json");
        UrlParser urlParser = new UrlParser();
        String storageName = urlParser.getStorageName(url);

        assertNotEquals("", storageName);
        assertEquals("p01d15201500002", storageName);
    }

    @Test
    public void getContainerTenantNameFromValidUrl() throws URISyntaxException  {
        URI url = new URI("https://p01d15201500002.blob.core.windows.net/alex2/trades-v1-27a8371b-9317-43ed-82c0-39835cf1ec03/2020-05-15T06:23:56-01:00.json");
        UrlParser urlParser = new UrlParser();
        String containerName = urlParser.getContainerTenantName(url);

        assertNotEquals("", containerName);
    }
}
