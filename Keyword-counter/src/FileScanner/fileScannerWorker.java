package FileScanner;

import MainCLI.PropertyConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.RecursiveTask;

public class fileScannerWorker extends RecursiveTask<Map<String, Integer>> {


    private volatile int start;
    private volatile int end;
    private File[] corpus;
    private Map<String,Integer> result;

    public fileScannerWorker(File[] corpus, int start, int end) {


        this.corpus = corpus;
        this.start = start;
        this.end = end;
        this.result = new HashMap<String,Integer>();
        for (String s: PropertyConstants.keywords)
            result.put(s.toLowerCase(),0);

    }

    @Override
    protected Map<String, Integer> compute() {

        if(start>end)
            return result;
        long currentSum = 0;
        int newStart = end+1;
        int thisThreadEnd = end;
        for (int i = start;i<=end;i++) {
            if((corpus[i].canRead())&&(corpus[i].isFile())&&(corpus[i].getName().endsWith(".txt"))) {
                currentSum += corpus[i].length();
                if (currentSum >= PropertyConstants.file_scanning_size_limit) {
                    newStart = i + 1;
                    thisThreadEnd = i;
                    break;
                }
            }
            else
                System.out.println("File " + "'"+corpus[i].getName()+"'" + " is not readable");
        }
        fileScannerWorker right = new fileScannerWorker(corpus,newStart,end);
        right.fork();
        Map<String, Integer> leftResult  = calculate(thisThreadEnd);
        Map<String, Integer> rightResult = right.join();
        return mergeMaps(leftResult,rightResult);

    }

    private Map<String, Integer> calculate(int end)  {
        for(int j = start;j<=end;j++){
            if((corpus[j].canRead())&&corpus[j].isFile()&&corpus[j].getName().endsWith(".txt"))
            {
                Scanner file= null;
                try {
                    file = new Scanner(corpus[j]);
                } catch (FileNotFoundException e) {
                    System.out.println("File " + "'"+corpus[j].getName()+"'" + " is not readable");
                }
                if(file!=null){
                    while(file.hasNext()){
                        String word=file.next().replaceAll("[^a-zA-Z]","").toLowerCase();
                        if(result.containsKey(word)){
                            result.put(word, result.get(word)+1);
                        }

                    }
                    file.close();
                }

            }
            else
                System.out.println("File " + "'"+corpus[j].getName()+"'" + " is not readable");
        }
        return result;
    }

    public Map<String,Integer> mergeMaps(Map<String,Integer> map1, Map<String,Integer> map2){
        Map<String,Integer> map3 = new HashMap<String,Integer>(map1);
        for (Map.Entry<String, Integer> e : map2.entrySet())
            map3.merge(e.getKey(), e.getValue(), Integer::sum);
        return map3;
    }
}
