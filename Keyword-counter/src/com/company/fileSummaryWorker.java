package com.company;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class fileSummaryWorker implements Callable<Map<String, Map<String, Integer>>> {

    private Map<String, Future<Map<String, Integer>>> fileCorpuses;
    private Map<String, Map<String, Integer>> fileSummary;

    public fileSummaryWorker(Map<String, Future<Map<String, Integer>>> fileCorpuses) {
        this.fileCorpuses = fileCorpuses;
        this.fileSummary = new ConcurrentHashMap<String,Map<String,Integer>>();
    }

    @Override
    public Map<String, Map<String, Integer>> call() throws Exception {

        for( Map.Entry<String, Future<Map<String, Integer>>> entry : fileCorpuses.entrySet() ){
            fileSummary.put(entry.getKey(),entry.getValue().get());
        }
        return fileSummary;
    }
}
