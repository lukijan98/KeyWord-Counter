package com.company;


public class JobDispatcherWorker implements Runnable {

    private ResultRetriever resultRetriever;

    public JobDispatcherWorker(ResultRetriever resultRetriever) {
        this.resultRetriever = resultRetriever;
    }

    @Override
    public void run() {
        while(true)
        {
            try {
                ScanningJob job = Main.jobQueue.take();
                System.out.println("Dispatcher took "+job.getQuery());
                resultRetriever.addCorpusResult(job.getQuery(), job.initiate());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
