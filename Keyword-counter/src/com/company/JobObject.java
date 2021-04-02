package com.company;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Future;

public class JobObject implements ScanningJob{

    private ScanType type;
    private String url;
    private File corpus;
    private boolean poison;
    private int jumpsLeft;

    public JobObject(String url,int jumpsLeft) {
        this.type = ScanType.WEB;
        this.url = url;
        this.jumpsLeft = jumpsLeft;
    }

    public JobObject(File corpus){
        this.type = ScanType.FILE;
        this.corpus = corpus;
    }

    @Override
    public ScanType getType() {
        return type;
    }

    @Override
    public String getQuery() {
        if(type.equals(ScanType.WEB))
            return url;
        if(type.equals(ScanType.FILE))
            return corpus.getName();
        return null;
    }

    @Override
    public Future<Map<String, Integer>> initiate() {
        if(type.equals(ScanType.WEB))
            return Main.webScannerThreadPool.submit(new webScannerWorker(url,jumpsLeft));
        if(type.equals(ScanType.FILE))
            return Main.fileScannerThreadPool.submit(new fileScannerWorker(corpus.listFiles(),0,corpus.listFiles().length-1));

        return null;
    }

    private boolean testQuery(){
        return true;
    }
}
