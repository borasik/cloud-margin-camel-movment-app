package org.ffdc.data.platform.Helpers;

import java.net.URI;

import org.springframework.stereotype.Component;

@Component
public class UrlParser {

    public String getStorageName(URI uriToParse)
    {
        String host = uriToParse.getHost();
        String[] hostArray = host.split("\\.");

        if(hostArray.length > 0)
        {
            return hostArray[0];
        }

        return "";
    }

    public String getContainerName(URI uriToParse)
    {
        String path = uriToParse.getPath().substring(1, uriToParse.getPath().length());
        String[] pathArray = path.split("\\/");

        if(pathArray.length > 0)
        {
            return pathArray[0];
        }

        return "";
    }

    public String getTenantName(URI uriToParse)
    {
        String path = uriToParse.getPath().substring(1, uriToParse.getPath().length());
        String[] pathArray = path.split("\\/");

        if(pathArray.length > 1)
        {
            return pathArray[1];
        }

        return "";
    }

    public String getDataSetId(URI uriToParse)
    {
        String path = uriToParse.getPath().substring(1, uriToParse.getPath().length());
        String[] pathArray = path.split("\\/");

        if(pathArray.length > 2)
        {
            return pathArray[2];
        }

        return "";
    }

    public String getFullFileName(URI uriToParse)
    {
        String path = uriToParse.getPath().substring(1, uriToParse.getPath().length());
        String[] pathArray = path.split("\\/");

        if(pathArray.length > 3)
        {
                return pathArray[3];
        }

        return "";
    }

    public String getFileName(URI uriToParse)
    {
        String path = uriToParse.getPath().substring(1, uriToParse.getPath().length());
        String[] pathArray = path.split("\\/");

        if(pathArray.length > 3)
        {
            String[] fileNameSplitted = pathArray[3].split("\\.");
            if(fileNameSplitted.length > 1)
            {
                return fileNameSplitted[0] + "." + fileNameSplitted[1];
            }
        }

        return "";
    }

    public String getFileExtension(URI uriToParse)
    {
        String path = uriToParse.getPath().substring(1, uriToParse.getPath().length());
        String[] pathArray = path.split("\\/");

        if(pathArray.length > 2)
        {
            String[] fileNameSplitted = pathArray[3].split("\\.");
            if(fileNameSplitted.length > 0)
            {
                return fileNameSplitted[fileNameSplitted.length - 1];
            }
        }

        return "";
    }
}
