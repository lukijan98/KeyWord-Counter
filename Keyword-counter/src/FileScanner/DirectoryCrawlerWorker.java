package FileScanner;

import Job.JobObject;
import MainCLI.Main;
import MainCLI.PropertyConstants;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class DirectoryCrawlerWorker implements Runnable {

    private HashMap<File,Long> filesLastModifedMap;
    private LinkedBlockingQueue<CrawlerObject> pathsToScan;
    private boolean shutdown;

    public DirectoryCrawlerWorker(LinkedBlockingQueue<CrawlerObject> pathsToScan) {
        this.filesLastModifedMap = new HashMap<>();
        this.pathsToScan = pathsToScan;
        this.shutdown = false;
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
                    CrawlerObject crawlerObject = pathsToScan.poll();
                    if(crawlerObject.isPoison())
                    {
                        shutdown = true;
                        break;
                    }
                    String path = crawlerObject.getPath();
                    File directory = new File(path);
                    if(!directory.exists())
                    {
                        System.out.println("Error: Path " + "'"+path+"' "+"does not exist");
                        continue;
                    }
                    try {
                        crawlDirectories(directory);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pathsToScan.add(crawlerObject);
                }
                if(shutdown)
                    break;
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
