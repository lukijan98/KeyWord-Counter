package com.company;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    public static void main(String[] args) throws InterruptedException {
	// write your code here
        File directoryPath = new File("example");
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
        Thread t = new Thread(new DirectoryCrawlerWorker(queue,"corpus_",1000));
        t.start();
        queue.add("example");
        t.join();
       // System.out.println(directoryPath.lastModified());
        //Thread.sleep(12000);
        //System.out.println(directoryPath.lastModified());
       // File a = new File("example/data/corpus_riker/text1.txt");
        //System.out.println(directoryPath.lastModified()==a.lastModified());
       // System.out.println(directoryPath.equals(a));
        //crawlDirectories(directoryPath);

    }
//    public static void crawlDirectories(File file){
//        if(file.isDirectory())
//        {
//            if(file.getName().startsWith("corpus_"))
//            {
//                System.out.println(file.getName());
//                return;
//            }
//            else
//            {
//                File[] files = file.listFiles();
//                for(File f:files)
//                {
//                    crawlDirectories(f);
//                }
//            }
//        }
//    }
}
