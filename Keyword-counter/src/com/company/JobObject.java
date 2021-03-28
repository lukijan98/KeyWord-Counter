package com.company;

import java.util.Map;
import java.util.concurrent.Future;

public class JobObject implements ScanningJob{

    private ScanType type;

    @Override
    public ScanType getType() {
        return null;
    }

    @Override
    public String getQuery() {
        return null;
    }

    @Override
    public Future<Map<String, Integer>> initiate() {
        return null;
    }
}
