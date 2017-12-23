
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class compareAlgorithms {	
	// Parses the passed document to find text within the passed tag
	// Refereed "https://docs.oracle.com/javase/tutorial/essential/regex/" to understand how to parse the given files using regex
	public static String getString(String docStr, String matchStr) {
		StringBuilder strbldr = new StringBuilder();    			
    	Pattern pattern = Pattern.compile(matchStr);	
	    Matcher matcher = pattern.matcher(docStr);  
	    	while (matcher.find()) {
	      	String topMatch = matcher.group(1);
	       	strbldr.append(topMatch);
	       	strbldr.append(" ");   	
	   	    }
	   	    if (matcher.find(1)) 
	  	       return strbldr.toString();
		    return null;
			
		}
	
	public static void main(String[] args) throws ParseException {
	try {
		Scanner src = new Scanner(System.in);
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("C:\\Users\\AVIATOR\\Desktop\\IU\\Sem1\\SEARCH\\Assignment\\Assignment 2\\index")));
		String sourceDir = "C:\\Users\\AVIATOR\\Desktop\\IU\\Sem1\\SEARCH\\Assignment\\Assignment 2\\topics.51-100"; 
		IndexSearcher searcher = new IndexSearcher(reader);
		// Get the preprocessed query terms
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		//the total number of documents in the corpus
		System.out.println("Enter Retrieval Algorithm Choice:");
		System.out.println("0:DefaultSimilarity\n1:BM25\n2:LMDirichletSimilarity\n3:LMJelinekMercerSimilarity");
		int choice = src.nextInt();
		String model= new String();
		switch(choice) {
			case 0 : searcher.setSimilarity(new ClassicSimilarity());
					 model = "Default";
					 break;
			case 1 : searcher.setSimilarity(new BM25Similarity());
					 model = "BM25";
					 break;
			case 2 : searcher.setSimilarity(new LMDirichletSimilarity());
					 model = "LMDirichlet";
					 break;
			case 3 : searcher.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7));
					 model = "LMJelinekMercer";
					 break;
			default : System.out.println("Entered wrong choice of algorithm. Please select the correct choice as mentioned above");
					  System.exit(0);
		}
		StringBuilder stringBuilder1 = new StringBuilder();
		stringBuilder1.append("QueryID"+" "+"Q0"+"  "+"DocID"+" 	   "+"Rank"+"  "+"Score"+"	     "+"RunID");
		stringBuilder1.append(System.getProperty("line.separator"));
		StringBuilder stringBuilder2 = new StringBuilder();
		stringBuilder2.append("QueryID"+" "+"Q0"+"  "+"DocID"+" 	   "+"Rank"+"  "+"Score"+"	     "+"RunID");
		stringBuilder2.append(System.getProperty("line.separator"));
		File files =new File(sourceDir);
		FileReader fileReader= new FileReader(files);
		String textFile = "";
		StringBuilder stringBuilder = new StringBuilder();
		String line = "";
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(" ");
		}
		bufferedReader.close();
		//stored content of file in string variable
		textFile = stringBuilder.toString();
		String[] doclist = textFile.split("<top>");
		for(int j=1; j <doclist.length; j++) {	
			String file = doclist[j];
			StringBuilder short_query= new StringBuilder();
			StringBuilder long_query= new StringBuilder();
			String queryID=new String();
			String matchedStr = getString(file,"<num>(.*?)<dom>");
			if (matchedStr != null) {
				String[] doc = matchedStr.split(":");
				queryID = doc[1].trim();
			}
			matchedStr = getString(file,"<title>(.*?)<desc>");
			if (matchedStr != null) {
				String[] doc = matchedStr.split(":");
				short_query.append(doc[1].replaceAll(" +", " ").trim());
				}	
			matchedStr = getString(file,"<desc>(.*?)<smry>");
			if (matchedStr != null) {
				String[] doc = matchedStr.split(":");
				long_query.append(doc[1].replaceAll(" +", " ").trim());
				}
			Query query = parser.parse(QueryParser.escape(short_query.toString()));
			TopDocs hits = searcher.search(query, 1000);
			ScoreDoc[] scoreDocs = hits.scoreDocs;
			for (int n = 0; n < scoreDocs.length; ++n){
				ScoreDoc sd = scoreDocs[n];
				stringBuilder1.append(queryID+"     "+"Q0"+"  "+searcher.doc(sd.doc).get("DOCNO")+"  "+(n+1)+"     "+(float) sd.score+"  "+"AN");
				stringBuilder1.append(System.getProperty("line.separator"));
				}
			Query query1 = parser.parse(QueryParser.escape(long_query.toString()));
			TopDocs hits1 = searcher.search(query1, 1000);
			ScoreDoc[] scoreDocs1 = hits1.scoreDocs;
			for (int n = 0; n < scoreDocs1.length; ++n){
				ScoreDoc sd = scoreDocs1[n];
				stringBuilder2.append(queryID+"     "+"Q0"+"  "+searcher.doc(sd.doc).get("DOCNO")+"  "+(n+1)+"     "+(float) sd.score+"  "+"AN");
				stringBuilder2.append(System.getProperty("line.separator"));
				}	
		}	
		int stringLen = stringBuilder1.toString().length();
		int stringLen1 = stringBuilder2.toString().length();
		FileOutputStream fileOutputStream = new FileOutputStream(new File(model+"shortQuery.txt"));
		fileOutputStream.write(stringBuilder1.toString().getBytes(), 0, stringLen);
        fileOutputStream.close();
        FileOutputStream fileOutputStream1 = new FileOutputStream(new File(model+"longQuery.txt"));
		fileOutputStream1.write(stringBuilder2.toString().getBytes(), 0, stringLen1);
        fileOutputStream1.close();
		src.close();    
        reader.close();
        System.out.println("Execution Ended");    
	}	
	catch (FileNotFoundException e) {
	 	System.out.print("file not found.");
	  	e.printStackTrace();
	}
	catch (IOException e) {
		e.printStackTrace();
	}
  }
}