package WebScanner;


import Job.JobObject;
import MainCLI.Main;
import MainCLI.PropertyConstants;
import ResultRetriever.ResultRetrieverImpl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class webScannerWorker implements Callable<Map<String,Integer>> {

    private Map<String,Integer> result;
    private String query;
    private int hopsLeft;

    public webScannerWorker(String query, int hopsLeft) {
        this.result = new HashMap<String, Integer>();
        for (String s: PropertyConstants.keywords)
            result.put(s.toLowerCase(),0);
        this.query = query;
        this.hopsLeft = hopsLeft;
    }

    @Override
    public Map<String, Integer> call()  {
        Document doc = null;
        try {
            doc = Jsoup.connect(query).get();
        } catch (IOException e) {
           // e.printStackTrace();
            System.out.println("Site unreachable: "+query);
            return result;
        }
        ((ResultRetrieverImpl) Main.resultRetriever).checkTimeForReset();
        if(hopsLeft!=0)
        {
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                if(!((ResultRetrieverImpl)Main.resultRetriever).isUrlInCorpus(link.attr("abs:href")))
                    Main.jobQueue.add(new JobObject(link.attr("abs:href"),hopsLeft-1));
            }
        }
        String siteText = doc.text().toLowerCase();
        for(String keyword: PropertyConstants.keywords)
        {
            int i = 0;
            Pattern p = Pattern.compile(keyword);
            Matcher m = p.matcher(siteText);
            while (m.find()) {
                i++;
            }
            result.put(keyword,result.get(keyword)+i);
        }

        return result;
    }
}
