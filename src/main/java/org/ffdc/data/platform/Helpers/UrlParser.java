package org.ffdc.data.platform.Helpers;

import java.net.URI;

import org.springframework.stereotype.Component;

@Component
public class UrlParser {  

    public String getStorageName(URI uriToParse) {
        String host = uriToParse.getHost();
        String[] hostArray = host.split("\\.");

        if(hostArray.length > 0)
        {
            return hostArray[0];
        }

        return "";
    }

    public String getContainerTenantName(URI uriToParse){
        String path = uriToParse.getPath().substring(1, uriToParse.getPath().length());
        String[] pathArray = path.split("\\/");

        if(pathArray.length > 0)
        {
            return pathArray[0];
        }

        return "";
    }

    public String getDataSetId(URI uriToParse){
        String path = uriToParse.getPath();
        String[] pathArray = path.split("\\/");

        if(pathArray.length > 1)
        {
            return pathArray[1];
        }

        return "";
    }

    public String getFileName(URI uriToParse){
        String path = uriToParse.getPath();
        String[] pathArray = path.split("\\/");

        if(pathArray.length > 2)
        {
            return pathArray[2];
        }

        return "";
    }
}
