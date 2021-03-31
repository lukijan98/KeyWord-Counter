package com.company;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ResultRetrieverImpl implements ResultRetriever{

    private Map<String, Future<Map<String, Integer>>> fileCorpuses;
    private Map<String, Future<Map<String, Integer>>> webCorpuses;
    private Future<Map<String, Map<String, Integer>>> fileSummary;
    private Future<Map<String, Map<String, Integer>>> webSummary;

    public ResultRetrieverImpl(){
        this.fileCorpuses = new ConcurrentHashMap<String, Future<Map<String, Integer>>>();
        this.webCorpuses  = new ConcurrentHashMap<String, Future<Map<String, Integer>>>();
        this.fileSummary  = null;
        this.webSummary   = null;
    }

    @Override
    public Map<String, Integer> getResult(String query) {
        String[] splittedQuery = query.split("\\|");
        ScanType scanType = ScanType.valueOf(splittedQuery[0]);

        Map<String, Future<Map<String, Integer>>> requestedCorpus = null;
        if (scanType.equals(ScanType.WEB)){
            requestedCorpus = webCorpuses;
        }
        if (scanType.equals(ScanType.FILE)){
            requestedCorpus = fileCorpuses;
        }
        if(requestedCorpus.containsKey(splittedQuery[1]))
        {
            try {
                return requestedCorpus.get(splittedQuery[1]).get();
            } catch (InterruptedException e) {

            } catch (ExecutionException e) {

            }
        }
        else
            System.out.println(splittedQuery[1]+" does not exist in the corpuses");
        return null;
    }

    @Override
    public Map<String, Integer> queryResult(String query) {
        ScanType scanType = ScanType.valueOf(query.split("|")[0]);
        Map<String, Future<Map<String, Integer>>> requestedCorpus = null;
        if (scanType.equals(ScanType.WEB)){
            requestedCorpus = webCorpuses;
        }
        if (scanType.equals(ScanType.FILE)){
            requestedCorpus = fileCorpuses;
        }
        if(requestedCorpus.containsKey(query))
        {
            if(requestedCorpus.get(query).isDone()) {
                try {
                    return requestedCorpus.get(query).get();
                } catch (InterruptedException e) {

                } catch (ExecutionException e) {

                }
            }
            System.out.println("Error : "+query +" is not yet ready, try again later");
        }
        else
            System.out.println("Error : "+query+" does not exist in the corpuses");
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
                    return Main.resultRetrieverThreadPool.submit(new fileSummaryWorker(fileCorpuses)).get();
                } catch (InterruptedException e) {

                } catch (ExecutionException e) {

                }
            }
        }
        System.out.println("getSummary doesn't work");
        return null;
    }

    @Override
    public Map<String, Map<String, Integer>> querySummary(ScanType summaryType) {
        return null;
    }

    @Override
    public void addCorpusResult(String corpusName, Future<Map<String, Integer>> corpusResult) {
        fileCorpuses.put(corpusName,corpusResult);
    }
}
