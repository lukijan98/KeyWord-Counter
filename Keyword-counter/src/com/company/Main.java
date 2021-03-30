package com.company;


import java.io.File;
import java.util.Map;
import java.util.concurrent.*;

public class Main {

    public static final ForkJoinPool fileScannerThreadPool ;
    public static final ExecutorService webScannerThreadPool;
    public static final BlockingQueue<JobObject> jobQueue;


    static {
        webScannerThreadPool = Executors.newCachedThreadPool();
        fileScannerThreadPool = new ForkJoinPool();
        jobQueue = new LinkedBlockingQueue<JobObject>();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        File directoryPath = new File("example/data2/subdir/corpus_mcfly");
        fileScannerWorker fsw = new fileScannerWorker(directoryPath.listFiles(),0,directoryPath.listFiles().length-1);
        Future<Map<String,Integer>> mapa1 = fileScannerThreadPool.submit(fsw);

        Map<String,Integer> mapa = mapa1.get();
        for( Map.Entry<String, Integer> entry : mapa.entrySet() ){
            System.out.println( entry.getKey() + " => " + entry.getValue() );
        }
//        for(String s:PropertyConstants.keywords)
//            System.out.println(s);
//        //System.out.println(keywords[2]);
//        System.out.println(PropertyConstants.file_corpus_prefix);
        //File directoryPath = new File("example");
//        LinkedBlockingQueue<String> queue1 = new LinkedBlockingQueue<String>();
//        LinkedBlockingQueue<String> queue2 = new LinkedBlockingQueue<String>();
//        Thread t1 = new Thread(new DirectoryCrawlerWorker(queue1));
//        Thread t2 = new Thread(new DirectoryCrawlerWorker(queue2));
//        t1.start();
//        t2.start();
//        queue1.add("example/data");
//        queue2.add("example/data2");

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
