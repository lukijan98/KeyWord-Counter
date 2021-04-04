package com.company;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class webSummaryWorker implements Callable<Map<String, Map<String, Integer>>> {

    private Map<String, Future<Map<String, Integer>>> webCorpuses;
    private Map<String,Map<String,Integer>> result;

    public webSummaryWorker(Map<String, Future<Map<String, Integer>>> webCorpuses) {
        this.webCorpuses = webCorpuses;
        result = new HashMap<String,Map<String,Integer>>();
    }

    @Override
    public Map<String, Map<String, Integer>> call() throws Exception {
        for( Map.Entry<String, Future<Map<String, Integer>>> entry_url : webCorpuses.entrySet() ){
            String domain = null;
            try{
                domain = getDomainName(entry_url.getKey());
            }
            catch (Exception e){
                continue;
            }
            if(result.containsKey(domain)){
                for( Map.Entry<String, Integer> entry_keywords : entry_url.getValue().get().entrySet() ){
                    result.get(domain).put(entry_keywords.getKey(),result.get(domain).get(entry_keywords.getKey())+entry_keywords.getValue());
                }
            }
            else
                result.put(domain,entry_url.getValue().get());
        }


        return result;
    }

    public String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
}
