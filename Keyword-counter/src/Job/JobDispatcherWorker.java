package Job;


import MainCLI.Main;
import ResultRetriever.ResultRetrieverImpl;

public class JobDispatcherWorker implements Runnable {



    @Override
    public void run() {
        while(true)
        {
            try {
                ScanningJob job = Main.jobQueue.take();
                if(((JobObject)job).isPoison())
                    break;
                if(job.getType().equals(ScanType.WEB))
                {
                    if(!((ResultRetrieverImpl)Main.resultRetriever).isUrlInCorpus(job.getQuery()))
                        Main.resultRetriever.addCorpusResult(job.getQuery(), job.initiate(),job.getType());
                }
                else
                    Main.resultRetriever.addCorpusResult(job.getQuery(), job.initiate(),job.getType());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
