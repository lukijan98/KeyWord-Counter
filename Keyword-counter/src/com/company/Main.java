package com.company;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {

    public static final ForkJoinPool fileScannerThreadPool ;
    public static final ExecutorService webScannerThreadPool;
    public static final ExecutorService resultRetrieverThreadPool;
    public static final BlockingQueue<ScanningJob> jobQueue;
    public static final ResultRetriever resultRetriever;

    static {
        webScannerThreadPool = Executors.newCachedThreadPool();
        fileScannerThreadPool = new ForkJoinPool();
        resultRetrieverThreadPool = Executors.newCachedThreadPool();
        jobQueue = new LinkedBlockingQueue<ScanningJob>();
        resultRetriever = new ResultRetrieverImpl();
    }

    public static void main(String[] args) throws  InterruptedException {
        File f= new File("asd");
        if(f.exists())
            System.out.println("postoji");
        LinkedBlockingQueue<CrawlerObject> pathsToScan = new LinkedBlockingQueue<>();
        Thread jobDispatcher = new Thread(new JobDispatcherWorker());
        jobDispatcher.start();
        Thread directoryCrawler = new Thread(new DirectoryCrawlerWorker(pathsToScan));
        directoryCrawler.start();
        Scanner sc = new Scanner(System.in);
        while(true){
            String line = sc.nextLine().trim();
            if(line.equals("stop")){
                pathsToScan.put(new CrawlerObject());
                System.out.println("Shutting down Directory Crawler");
                jobQueue.put(new JobObject());
                System.out.println("Shutting down Job Queue");
                fileScannerThreadPool.shutdown();
                System.out.println("Shutting down File Scanner");
                webScannerThreadPool.shutdown();
                System.out.println("Shutting down Web Scanner");
                resultRetrieverThreadPool.shutdown();
                System.out.println("Shutting down Result Retriever");
                while((!fileScannerThreadPool.isTerminated())||(!webScannerThreadPool.isTerminated())||
                        (!resultRetrieverThreadPool.isTerminated()));
                System.out.println("Program has ended");
                break;
            }else
            if(line.equals("cfs"))
                resultRetriever.clearSummary(ScanType.FILE);
            else if(line.equals("cws"))
                resultRetriever.clearSummary(ScanType.WEB);
            else if(line.startsWith("aw ")){
                line = line.substring(3).trim();
                if(isValidURL(line))
                    jobQueue.put(new JobObject(line,PropertyConstants.hop_count));
                else
                    System.out.println("Error: Invalid url '"+line+"'");
            }else if(line.startsWith("ad ")){
                line = line.substring(3).trim();
                pathsToScan.put(new CrawlerObject(line));
            }else if(line.startsWith("query ")){
                line = line.substring(6).trim();
                String[] splittedQuery = line.split("\\|");
                ScanType scanType = null;
                try {
                    scanType = ScanType.valueOf(splittedQuery[0].trim().toUpperCase());
                    if(!(splittedQuery[1].trim().equals("summary"))){
                        Map<String, Integer> result = resultRetriever.queryResult(line);
                        if(result!=null)
                            printResult(result);
                    }else
                    {
                        Map<String, Map<String, Integer>> result = resultRetriever.querySummary(scanType);
                        if (result != null)
                            printSummaryResult(result);
                    }
                }catch (Exception e){
                    System.out.println("Error: Unsupported Scan type");
                }
            }else if(line.startsWith("get ")){
                line = line.substring(4).trim();
                String[] splittedQuery = line.split("\\|");
                ScanType scanType = null;
                try {
                    scanType = ScanType.valueOf(splittedQuery[0].trim().toUpperCase());
                    if(!(splittedQuery[1].trim().equals("summary"))){
                        Map<String, Integer> result = resultRetriever.getResult(line);
                        if(result!=null)
                            printResult(result);
                    }else
                    {
                        Map<String, Map<String, Integer>> result = resultRetriever.getSummary(scanType);
                        if (result != null)
                            printSummaryResult(result);
                    }
                }catch (Exception e){
                    System.out.println("Error: Unsupported Scan type");
                }
            }
            else
                System.out.println("Error: Unsupported command '"+line+"'");


        }
        sc.close();

    }
    public static void printResult(Map<String, Integer> result){
        String resultString = "{";
        for( Map.Entry<String, Integer> entry : result.entrySet() ){
            resultString+=(entry.getKey() + '=' + entry.getValue()+',');
        }
        System.out.println(resultString.substring(0,resultString.length()-1)+"}");
    }

    public static void printSummaryResult(Map<String, Map<String, Integer>> result){
        for( Map.Entry<String, Map<String, Integer>> entry : result.entrySet() ){
            String resultString = entry.getKey()+"  {";
            for( Map.Entry<String, Integer> entry1 : entry.getValue().entrySet() ){
                resultString+=(entry1.getKey() + '=' + entry1.getValue()+',');
            }
            System.out.println(resultString.substring(0,resultString.length()-1)+"}");
        }
    }

    public static boolean isValidURL(String url)
    {
        try {
            new URL(url).toURI();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

}
