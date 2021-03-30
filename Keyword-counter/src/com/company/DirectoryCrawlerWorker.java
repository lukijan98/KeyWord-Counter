package com.company;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class DirectoryCrawlerWorker implements Runnable {

    private HashMap<File,Long> filesLastModifedMap;
    private LinkedBlockingQueue<String> pathsToScan;

    public DirectoryCrawlerWorker(LinkedBlockingQueue<String> pathsToScan) {
        this.filesLastModifedMap = new HashMap<>();
        this.pathsToScan = pathsToScan;
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
                    try {
                        crawlDirectories(directory);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pathsToScan.add(path);
                }
            }
            try {
                Thread.sleep(PropertyConstants.dir_crawler_sleep_time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void crawlDirectories(File file) throws InterruptedException {
        if(file.isDirectory())
        {
            if(file.getName().startsWith(PropertyConstants.file_corpus_prefix))
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

    private void createJob(File file) throws InterruptedException {
//        File[] files = file.listFiles();
//        for(File f:files)
//        {
//            System.out.println(f.getName());
//        }
        Main.jobQueue.put(new JobObject(file));

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
