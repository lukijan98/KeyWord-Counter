package com.company;

import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;


public class PropertyConstants {
    public static final CopyOnWriteArrayList<String> keywords;
    public static final String file_corpus_prefix;
    public static final long dir_crawler_sleep_time;
    public static final long file_scanning_size_limit;
    public static final int hop_count;
    public static final long url_refresh_time;

    static {
        java.util.Properties prop = null;
        try {
            prop = readPropertiesFile("app.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
        keywords = new CopyOnWriteArrayList<>(prop.getProperty("keywords").split(","));
        file_corpus_prefix = prop.getProperty("file_corpus_prefix");
        dir_crawler_sleep_time = Long.parseLong(prop.getProperty("dir_crawler_sleep_time"));
        file_scanning_size_limit = Long.parseLong(prop.getProperty("file_scanning_size_limit"));
        hop_count = Integer.parseInt(prop.getProperty("hop_count"));
        url_refresh_time = Long.parseLong(prop.getProperty("url_refresh_time"));
    }

    public static java.util.Properties readPropertiesFile(String fileName) throws IOException {
        java.util.Properties prop = new java.util.Properties();
        FileReader reader=new FileReader(fileName);
        prop.load(reader);
        return prop;
    }
}
