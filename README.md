# KeyWord Counter

Project for class "Concurrent and Distributed programming" at Računarski fakultet

## 1 Overview:

The keyword counter system should support counting the occurrences of keywords that are predefined in corpora of different types. This counting should be done concurrently, with the possibility of adding new corpora as well as reviewing the counting results for individual corpora and summarizing the results. For homework purposes, it will be necessary to implement work on ASCII encoded text files and HTML files located on the web. Keywords are counted in these corpora only when they stand alone, not when they are part of other words, and the search should be case-sensitive. The system should be fully implemented in Java programming language.
The system is divided into several components, with the proviso that it should be possible to add new components in the future relatively easily. Requirements for components and their mutual communication are given in section 2.
The system should gracefully resolve problematic work situations. It is important that the system informs the user when a problem occurs, as well as that it never completely collapses. Requirements for troubleshooting the system are given in section 3.
The user interacts with the system via the command line (CLI), by entering commands. In addition, the operation of the system will be configured via a configuration file. A description of this file, as well as a list of commands, their parameters, results, and possible errors, is given in Section 4. In this section, several examples of using the system will be given.
The task scoring as well as the instructions for submitting the task are given in section 5. 

## 2 System description:

The system consists of three components that are based on a thread pool, and several auxiliary components that run each in its own thread. They have their own thread pool:

     • Web Scanner
     
     • File Scanner
     
     • Result Retriever
     
While in individual threads are executed:

     • Main / CLI
     
     • Job dispatcher
     
     • Directory crawler
     
In addition to these active components, there is a shared queue that is used to assign new jobs and start them. 

### 2.1 Directory crawler thread

This component goes around the specified set of directories on disk, and looks for subdirectories that represent text corpora. The main component will use a special command to specify which directories the directory crawler should crawl, and which subdirectories represent the corpus will be specified in the configuration file using the directory name prefix.
The directory crawler should search for the specified directory (and enter subdirectories), until it finds a directory whose name begins with the specified prefix. When it finds such a directory, it assumes it is a corpus and creates a new Job to scan that directory and places it in the Job queue. At the same time, the directory crawler should record their "last modified" value for all files in the directory. If all the files in the directory already had a "last modified" value, and the current value is the same as the one previously written, then a new Job should not be started.
After crawling the specified directories, the directory crawler pauses for a while (the duration given in the configuration file), after which it scans the same set of directories again. 

### 2.2 Job queue

A job queue is a shared blocking line that contains job descriptions that need to be started. One job could (but does not have to) be described with the following interface: 

```
public interface ScanningJob {

	ScanType getType();
  
	String getQuery();
  
	Future<Map<String, Integer>> initiate();	
  
}
```

Where ScanType is an enumeration (FILE / WEB) that specifies the type of job, query is a query that will retrieve the results of this job via the CLI, and initiate is the method that will start the job within the appropriate thread pool. The result of the initiate () method is a Future object that represents a count job for that corpus, and the result is a map that has exactly as many keys as there are keywords, and the number of occurrences associated with each corpus in that corpus.
The idea is that any component can write to this line, but currently these are Main / CLI (when starting the count for the Web corpus), Directory crawler (when starting the count for the text corpus) and Web scanner (when it finds a new URL within the HTML code ). This queue reads only the Job dispatcher component.

### 2.3 Job dispatcher thread

A job dispatcher is a thread that waits for a Job to appear, and then delegates it to the appropriate thread pool system (generally, a Job dispatcher can perform jobs in a different way, but both File and Web jobs will be done within the thread pool) . This thread should be blocked if there are no items in the queue, and not load the processor by constantly querying the queue.

### 2.4 Web/File scanner thread pool

When starting a Web scanner job, one HTML page is specified as the starting point, as well as the number of jumps that will be made from that page (when the job is first created, the number of jumps is taken from the configuration file). This component needs to do two things in one job:

    1. Count the keyword impressions for a given page and report it to the Result retriever component.
    
    2. If the specified number of jumps for this job is greater than zero, then it should search the HTML document for other URLs, and create new jobs to view those URLs. All new jobs created in this way will have a number of jumps that is reduced by 1 compared to the current one. These jobs are entered in the Job queue. If the found URL has already been scanned, then skip it. The set of scanned URLs should be automatically deleted after the period specified in the configuration file.
    
Note: for downloading HTML pages, as well as their parsing (in order to find links), the use of a library is allowed, and it is recommended, such as. Jsoup, which facilitates the process. It is not necessary to implement HTML parsing, nor to manually resolve URLs within HTML code. An example of listing all links within an HTML page can be found here.

When you start the File scanner job, a directory containing a set of text files is specified. This job should be divided into smaller jobs where one thread needs to process one or more files, and that set must have a cumulative size (in bytes) larger than the limit specified by the configuration file. We should strive to have as many threads as possible that process the corpus, but it is not necessary to find a work schedule for which the number of threads is maximum. It is allowed to assume that all files have approximately the same size, so that the work can be divided "greedily". As a result of this work, the number of occurrences of all keywords within the corpus is returned, and this result is stored in the Result retriever component.

### 2.5 Result retriever thread pool

This component stores the results, and has operations to retrieve the count results, as well as perform some simple operations on the results. Results are retrieved by querying (which will come from the Main / CLI component), and queries can take two forms:

#### 2.5.1 Number of keywords in the corpus

The simplest query returns the number of impressions of all keywords in a corpus, and has the form:

file | directory_name

web | domain_name

In the first variant, the direct result of the operation of the File scanner component for the specified directory, which represents the corpus, is returned.
In the second variant, the collective appearance of all keywords on pages that belong to the same domain is returned. The web scanner component gives the number of impressions of all keywords for one page, and when this query is asked, the Result retriever component should start the job of collecting all values for a given domain within its thread pool. Once this value is calculated, it should be stored so that it is obtained without recalculation on subsequent queries.

#### 2.5.2 Summary

The second form of the query allows retrieval of results for all corpora currently stored (if counting for a corpus is not completed, this job is waiting to be completed), and takes the form:

file | summary

web | summary

The construction of these results is done within the thread pool. As with regular queries, the result of this query should be stored for faster retrieval.

#### 2.5.3 Notes

The result retriever component should be able to retrieve results that are blocking (get) and non-blocking (query). In the second variant, you should either return the result if it is available, or return the information that the calculation of that corpus is not in progress if it does not exist at all, or the information that the calculation of that corpus is in progress if the calculation is not completed yet.

Result retriever should also provide operations to delete certain stored results, specifically:

     • Delete summary results (will be done via a command on the CLI).
     
     • Rewriting data for the text corpus (done automatically when a file on the disk is modified).
     
     • Delete data for an individual web domain. It is only allowed to mark a web domain result as "obsolete" when refreshing data for a page belonging to that domain. After that, when searching for data for that domain, the account is performed again.
     
The result retriever component could (but does not have to) be described by the following interface:

```
public interface ResultRetriever {

	public Map<String, Integer> getResult(String query);
  
	public Map<String, Integer> queryResult(String query);
  
	public void clearSummary(ScanType summaryType);
  
	public Map<String, Map<String, Integer>> getSummary(ScanType summaryType);
  
	public Map<String, Map<String, Integer>> querySummary(ScanType summaryType);
  
	public void addCorpusResult(String corpusName, Future<Map<String, Integer>> corpusResult);
  
}
```

### 2.6 Main/CLI thread

The main component starts executing our system. At startup, the settings are loaded from the configuration file, and then a command is expected from the user. A detailed description of this file, as well as the commands are given in section 4. For now, we only state in detail how this component is connected to the rest of the system:

     • Directory crawler
     
         ◦ The ad command is used to specify a new bypass directory. As part of this command, it will be necessary to add an item to the list of directories that
         the directory crawler visits.
         
     • Job queue
     
         ◦ The aw command is used to specify a new home page for web browsing. This command will create a new job for the web and put it directly in the job queue.
         
     • Result retriever
     
         ◦ There are several commands for retrieving system results. All of these commands will actually address the result retriever component and retrieve the results synchronously or asynchronously.
         
## 3 System and command setup

The user interacts with the system by entering commands at the command line. In addition, the operation of the system is additionally adjusted using a configuration file.

### 3.1 Configuration file

The system is configured using the app.properties configuration file, which has the following parameters:

#list of keywords searched in corpora, separated by commas.\
#keywords are not counted in corpora if they are part of another word,\
#but only when they stand alone in the text\
keywords=one,two,three

#prefix for directories containing text corpora\
file_corpus_prefix=corpus_

#pause period for directory crawler, given in ms\
dir_crawler_sleep_time=1000

#limit for file scanner component, given in bytes\
file_scanning_size_limit=1048576

#number of jumps that the web scanner component will make when searching\
hop_count=1

#time after which the set of visited urls is deleted, given in ms\
url_refresh_time=86400000

All these parameters are read once at the start of the application, and will not change during operation. The only way to change the values of these parameters is to stop the application completely and restart it.

### 3.2 Commands

The system supports the following commands:

Command name: ad\
Parameter: String\
Description: Add directory. Adds a new scan directory, which is passed to the Directory scanner component. The directory is located within the project, and is specified as a relative path. There can be arbitrarily many subdirectories within this directory, but some of them need to have a name that starts with the prefix specified in the configuration file (file_corpus_prefix), and they represent text corpora for our system.

Command name: aw\
Parameter: String\
Description: Add web. Adds a page from which a new tour should start. The number of jumps for this job is taken from the configuration file (hop_count). For each URL found on the page, a new job will be created that will have the number of jumps one less than the current one. For each page visited, keyword impressions are counted, and stored in the result retriever component.

Command name: get\
Parameter: String\
Description: Retrieves the result from the Result retriever component and prints it to the console. The query (described in section 2.5) is given as an argument. This command blocks further work until it gets the result.



Command name: query\
Parameter: String\
Description: Retrieves the result from the Result retriever component and prints it to the console. The query (described in section 2.5) is given as an argument. This command does not block further work, but only prints a message if the results are not available or if work for that query has not yet begun.

Command name: cws / cfs\
Parameter: -\
Description: Clear web summary / Clear file summary. These two commands are specified without an argument, and when they are specified, the Result retriever component should be reported to clear the corresponding summary result, if any.

Command name: stop\
Parameter: -\
Description: Shut down the application. Stops all thread pools and tells all threads to finish neatly. Do not use violent interrupts for this command.

### 3.3 Examples of usage

The examples assume that the configuration file listed in section 3.1 is used. Bold text represents typed commands, while plain text is a program print.

Example 1: adding a directory to scan and retrieve results. The directory given within the project contains the subdirectories **corpus_a** and **corpus_a2** somewhere within its structure and these directories contain text files to scan.

**ad data**\
Adding dir /home/example/data\
Starting file scan for file|corpus_a2\
Starting file scan for file|corpus_a\
**get file|corpus_a**\
{one=56, two=41, three=8}\
**stop**\
Stopping...

Example 2: adding a web page to tour and read web summary results.

**aw https://www.gatesnotes.com/2019-Annual-Letter** \
Starting web scan for web|https://www.gatesnotes.com/2019-Annual-Letter \
Starting web scan for web|https://www.gatesnotes.com/ \
Starting web scan for web|https://www.gatesnotes.com/Books\ \
...\
Starting web scan for web|https://blog.23andme.com/23andme-research/new-study-finds-genetic-links-risk-premature-births/ \
Starting web scan for web|https://www.wired.com/story/wired25-bill-gates-stephen-quake-blood-tests/ \
**query web|summary**\
Summary is not ready yet\
**get web|summary**\
wired.com: {one=0, two=1, three=0}\
twitter.com: {one=0, two=0, three=0}\
blog.23andme.com: {one=0, two=1, three=1}\
windows.microsoft.com: {one=0, two=0, three=0}\
gatesnotes.com: {one=30, two=2, three=25}\
reddit.com: {one=0, two=0, three=0}\
