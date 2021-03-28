package com.company;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class DirectoryCrawlerWorker implements Runnable {

    private HashMap<File,Long> filesLastModifedMap;
    private LinkedBlockingQueue<String> pathsToScan;
    private String file_corpus_prefix;
    private long dir_crawler_sleep_time;

    public DirectoryCrawlerWorker(LinkedBlockingQueue<String> pathsToScan, String file_corpus_prefix, long dir_crawler_sleep_time) {
        this.filesLastModifedMap = new HashMap<>();
        this.pathsToScan = pathsToScan;
        this.file_corpus_prefix = file_corpus_prefix;
        this.dir_crawler_sleep_time = dir_crawler_sleep_time;
    }

    @Override
    public void run() {
        while(true)
        {
            if(!pathsToScan.isEmpty())
            {
                int numberOfPaths = pathsToScan.size();
                for (int i = 0; i < numberOfPaths; i++)
                {
                    String path = pathsToScan.poll();
                    File directory = new File(path);
                    crawlDirectories(directory);
                    pathsToScan.add(path);
                }
            }
            try {
                Thread.sleep(dir_crawler_sleep_time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void crawlDirectories(File file){
        if(file.isDirectory())
        {
            if(file.getName().startsWith(file_corpus_prefix))
            {
                if(checkDirectory(file))
                    createJob(file);
                return;
            }
            else
            {
                File[] files = file.listFiles();
                for(File f:files)
                {
                        crawlDirectories(f);
                }
            }
        }
    }

    private void createJob(File file){
        File[] files = file.listFiles();
        for(File f:files)
            System.out.println(f.getName());
    }

    private boolean checkDirectory(File file){
        File[] files = file.listFiles();
        boolean readyForJob = false;
        for (File f: files) {
            if(isModified(f)){
                readyForJob = true;
            }
        }
        return readyForJob;
    }

    private boolean isModified(File file)
    {
        if(filesLastModifedMap.containsKey(file))
        {
            if(file.lastModified() != filesLastModifedMap.get(file)){
                filesLastModifedMap.put(file,file.lastModified());
                return true;
            }
            return false;
        }
        filesLastModifedMap.put(file,file.lastModified());
        return true;
    }
}
