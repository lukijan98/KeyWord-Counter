package ResultRetriever;

import Job.ScanType;
import MainCLI.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ResultRetrieverImpl implements ResultRetriever{

    private Map<String, Future<Map<String, Integer>>> fileCorpuses;
    private Map<String, Future<Map<String, Integer>>> webCorpuses;
    private Future<Map<String, Map<String, Integer>>> fileSummary;
    private Future<Map<String, Map<String, Integer>>> webSummary;
    private static long resetTime;

    public ResultRetrieverImpl(){
        this.fileCorpuses = new ConcurrentHashMap<String, Future<Map<String, Integer>>>();
        this.webCorpuses  = new ConcurrentHashMap<String, Future<Map<String, Integer>>>();
        this.fileSummary  = null;
        this.webSummary   = null;
        this.resetTime = System.currentTimeMillis();
    }

    @Override
    public Map<String, Integer> getResult(String query) {
        String[] splittedQuery = query.split("\\|");
        ScanType scanType = ScanType.valueOf(splittedQuery[0].trim().toUpperCase());
        Map<String, Future<Map<String, Integer>>> requestedCorpus = null;
        if (scanType.equals(ScanType.WEB)){
            requestedCorpus = webCorpuses;
        }else
        if (scanType.equals(ScanType.FILE)){
            requestedCorpus = fileCorpuses;
        }

        if(requestedCorpus.containsKey(splittedQuery[1].trim()))
        {
            try {
                return requestedCorpus.get(splittedQuery[1].trim()).get();
            } catch (InterruptedException e) {

            } catch (ExecutionException e) {

            }
        }
        else
            System.out.println(splittedQuery[1].trim()+" does not exist in the corpuses");
        return null;
    }

    @Override
    public Map<String, Integer> queryResult(String query) {
        String[] splittedQuery = query.split("\\|");
        ScanType scanType = ScanType.valueOf(splittedQuery[0].trim().toUpperCase());;

        Map<String, Future<Map<String, Integer>>> requestedCorpus = null ;
        if (scanType.equals(ScanType.WEB)){
            requestedCorpus = webCorpuses;
        }else
        if (scanType.equals(ScanType.FILE)){
            requestedCorpus = fileCorpuses;
        }

        if(requestedCorpus.containsKey(splittedQuery[1].trim()))
        {
            if(requestedCorpus.get(splittedQuery[1].trim()).isDone()) {
                try {
                    return requestedCorpus.get(splittedQuery[1].trim()).get();
                } catch (InterruptedException e) {

                } catch (ExecutionException e) {

                }
            }
            System.out.println("Error : "+splittedQuery[1].trim() +" is not yet ready, try again later");
        }
        else
            System.out.println("Error : "+splittedQuery[1].trim()+" does not exist in the corpuses");
        return null;
    }

    @Override
    public void clearSummary(ScanType summaryType) {
        if(summaryType.equals(ScanType.FILE)) {
            try {
                if(fileSummary.isDone()&&fileSummary!=null){
                    fileSummary.get().clear();
                    fileSummary = null;
                    System.out.println("Success: File summary has been cleared");
                }
                else
                    System.out.println("Error: File summary can't be cleared while it's not yet done!");

            } catch (InterruptedException e) {

            } catch (ExecutionException e) {

            }
        }
        if(summaryType.equals(ScanType.WEB)) {
            try {
                if(webSummary!=null&&webSummary.isDone()){
                    webSummary.get().clear();
                    webSummary = null;
                    System.out.println("Success: Web summary has been cleared");
                }
                else
                    System.out.println("Error: web summary can't be cleared while it's not yet done!");

            } catch (InterruptedException e) {

            } catch (ExecutionException e) {

            }
        }
    }

    @Override
    public Map<String, Map<String, Integer>> getSummary(ScanType summaryType) {
        if(summaryType.equals(ScanType.FILE)){
            if(fileSummary!=null){
                try {
                    return fileSummary.get();
                } catch (InterruptedException e) {

                } catch (ExecutionException e) {

                }
            }
            else {
                try {
                    if(!fileCorpuses.isEmpty()){
                        fileSummary = Main.resultRetrieverThreadPool.submit(new fileSummaryWorker(fileCorpuses));
                        return fileSummary.get();
                    }

                    else
                    {
                        System.out.println("Can't start summary because corpuses is empty");
                        return null;
                    }
                } catch (InterruptedException e) {

                } catch (ExecutionException e) {

                }
            }
        }
        if(summaryType.equals(ScanType.WEB)){
            if(webSummary!=null){
                try {
                    return webSummary.get();
                } catch (InterruptedException e) {

                } catch (ExecutionException e) {

                }
            }
            else {
                try {
                    if (!webCorpuses.isEmpty()){
                        webSummary = Main.resultRetrieverThreadPool.submit(new webSummaryWorker(webCorpuses));
                        return webSummary.get();
                    }

                    else
                    {
                        System.out.println("Can't start summary because corpuses is empty");
                        return null;
                    }
                } catch (InterruptedException e) {
                        e.printStackTrace();
                } catch (ExecutionException e) {
                        e.printStackTrace();
                }

            }
        }
        System.out.println("getSummary doesn't work");
        return null;
    }

    @Override
    public Map<String, Map<String, Integer>> querySummary(ScanType summaryType) {
        if (summaryType.equals(ScanType.FILE)) {
            if (fileSummary != null) {
                if (fileSummary.isDone()) {
                    try {
                        return fileSummary.get();
                    } catch (InterruptedException e) {

                    } catch (ExecutionException e) {

                    }
                }
                System.out.println("Summary not yet done");
                return null;
            } else {
                if (!fileCorpuses.isEmpty()) {
                    fileSummary = Main.resultRetrieverThreadPool.submit(new fileSummaryWorker(fileCorpuses));
                    System.out.println("Summary not started calculating. Starting now...");
                }
                else
                    System.out.println("Summary not started calculating."+'\n'+"Can't start because corpus is empty");
                return null;
            }
        }
        if (summaryType.equals(ScanType.WEB)) {


            if (webSummary != null) {
                if (webSummary.isDone()) {
                    try {
                        return webSummary.get();
                    } catch (InterruptedException e) {

                    } catch (ExecutionException e) {

                    }
                }
                System.out.println("Summary not yet done");
                return null;

            } else {
                if (!webCorpuses.isEmpty()) {
                    webSummary = Main.resultRetrieverThreadPool.submit(new webSummaryWorker(webCorpuses));
                    System.out.println("Summary not started calculating. Starting now...");
                }
                else
                    System.out.println("Summary not started calculating."+'\n'+"Can't start because corpus is empty");

                return null;
            }
        }
        System.out.println("querySummary doesn't work");
        return null;
    }

    @Override
    public void addCorpusResult(String corpusName, Future<Map<String, Integer>> corpusResult, ScanType summaryType) {
        if(summaryType.equals(ScanType.FILE))
        {
            fileCorpuses.put(corpusName,corpusResult);
            System.out.println("Successfully added "+ corpusName+ " to corpus list");
            return;
        }

        if(summaryType.equals(ScanType.WEB))
        {
            webCorpuses.put(corpusName,corpusResult);
            System.out.println("Successfully added "+ corpusName+ " to corpus list");
            return;
        }
        System.out.println("Error: failed to add "+corpusName+ " to corpus list");
    }


    public boolean isUrlInCorpus(String url){
        return webCorpuses.containsKey(url);
    }

    public void checkTimeForReset(){
        if(System.currentTimeMillis()-resetTime>= PropertyConstants.url_refresh_time){
            resetTime = System.currentTimeMillis();
            webCorpuses.clear();
        }
    }
}
