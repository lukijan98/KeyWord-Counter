package com.company;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;


public class webScannerWorker implements Callable<Map<String,Integer>> {

    private Map<String,Integer> result;
    private String query;

    public webScannerWorker(String query) {
        this.result = new HashMap<String, Integer>();
        this.query = query;
    }

    @Override
    public Map<String, Integer> call() throws Exception {
        return null;
    }
}
