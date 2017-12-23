
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
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class searchTRECtopics {
// Refered https://stackoverflow.com/questions/11647889/sorting-the-mapkey-value-in-descending-order-based-on-the-value	to sort the Hash Map	
// Sorts the given hashmap by converting it to list and returns a list
	static <K,V extends Comparable<? super V>> 
    List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {
		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
		Collections.sort(sortedEntries, 
				new Comparator<Entry<K,V>>() {
				@Override
				public int compare(Entry<K,V> e1, Entry<K,V> e2) {
					return e2.getValue().compareTo(e1.getValue());					
					}
				}		    
		);
		return sortedEntries;
	}
		

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
	
	@SuppressWarnings("unchecked")
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
		int N = reader.maxDoc();
		Map<Integer, Float> short_relevantDoc = new HashMap<Integer, Float>();
		HashMap<Integer, Float> short_docLen = new HashMap<Integer, Float>();
		Map<Integer, Float> long_relevantDoc = new HashMap<Integer, Float>();
		HashMap<Integer, Float> long_docLen = new HashMap<Integer, Float>();
		//Use DefaultSimilarity.decodeNormValue(…) to decode normalized document length
		ClassicSimilarity dSimi = new ClassicSimilarity();
		// Get the segments of the index
		List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
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
			short_relevantDoc = new HashMap<Integer, Float>();
			long_relevantDoc = new HashMap<Integer, Float>();
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
			Set<Term> queryTerms = new LinkedHashSet<Term>();
			searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);			
			// Processing each segment
			for (int i = 0; i < leafContexts.size(); i++) {
				// Get document length
				LeafReaderContext leafContext = leafContexts.get(i);
				short_docLen = new HashMap<Integer, Float>();
				int startDocNo = leafContext.docBase;
				int numberOfDoc = leafContext.reader().maxDoc();
				for (int docId = 0; docId < numberOfDoc; docId++) {
					// Get normalized length (1/sqrt(numOfTokens)) of the document
					float normDocLeng = dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));
					// Get length of the document
					float docLeng = 1 / (normDocLeng * normDocLeng);
					short_docLen.put(docId+startDocNo, docLeng);
				}
				for (Term t : queryTerms) {
					int df=reader.docFreq(new Term("TEXT", t.text()));
					float idf = (float) Math.log((1+N/(float)df));
					PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),"TEXT", new BytesRef(t.text()));
					if (de != null) {
						while ((de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
							float weight =  (float) ((de.freq()/short_docLen.get(de.docID()+startDocNo))*idf);
							if(short_relevantDoc.containsKey(de.docID() +startDocNo)) {
								short_relevantDoc.put((de.docID() +startDocNo),short_relevantDoc.get(de.docID() +startDocNo) + weight);
							}
							else {
								 short_relevantDoc.put((de.docID() +startDocNo), weight);
								}
							}
						}
					}
				}
			Query query1 = parser.parse(QueryParser.escape(long_query.toString()));
			Set<Term> queryTerms1 = new LinkedHashSet<Term>();
			searcher.createNormalizedWeight(query1, false).extractTerms(queryTerms1);			
			// Processing each segment
			for (int i = 0; i < leafContexts.size(); i++) {
				// Get document length
				LeafReaderContext leafContext = leafContexts.get(i);
				long_docLen = new HashMap<Integer, Float>();
				int startDocNo = leafContext.docBase;
				int numberOfDoc = leafContext.reader().maxDoc();
				for (int docId = 0; docId < numberOfDoc; docId++) {
					// Get normalized length (1/sqrt(numOfTokens)) of the document
					float normDocLeng = dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));
					// Get length of the document
					float docLeng = 1 / (normDocLeng * normDocLeng);
					long_docLen.put(docId+startDocNo, docLeng);
				}
				for (Term t : queryTerms1) {
					int df=reader.docFreq(new Term("TEXT", t.text()));
					float idf = (float) Math.log((1+N/(float)df));
					PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),"TEXT", new BytesRef(t.text()));
					if (de != null) {
						while ((de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
							float weight =  (float) ((de.freq()/long_docLen.get(de.docID()+startDocNo))*idf);
							if(long_relevantDoc.containsKey(de.docID() +startDocNo)) {
								long_relevantDoc.put((de.docID() +startDocNo),long_relevantDoc.get(de.docID() +startDocNo) + weight);
							}
							else {
								 long_relevantDoc.put((de.docID() +startDocNo), weight);
								}
							}
						}
					}
				}
			// Call to the sort function
			List<Entry<Integer, Float>> sortedRelevantDoc = entriesSortedByValues(short_relevantDoc);
			List<Entry<Integer, Float>> sortedRelevantDoc1 = entriesSortedByValues(long_relevantDoc);
			for (int i=0; i<sortedRelevantDoc.size();i++) {
				if(i==1000) {
					break;
				}				
				stringBuilder1.append(queryID+"     "+"Q0"+"  "+searcher.doc(sortedRelevantDoc.get(i).getKey()).get("DOCNO")+"  "+(i+1)+"     "+sortedRelevantDoc.get(i).getValue()+"  "+"AN");
				stringBuilder1.append(System.getProperty("line.separator"));
			}
			for (int i=0; i<sortedRelevantDoc1.size();i++) {
				if(i==1000) {
					break;
				}				
				stringBuilder2.append(queryID+"     "+"Q0"+"  "+searcher.doc(sortedRelevantDoc1.get(i).getKey()).get("DOCNO")+"  "+(i+1)+"     "+sortedRelevantDoc1.get(i).getValue()+"  "+"AN");
				stringBuilder2.append(System.getProperty("line.separator"));
			}
		}	
		int stringLen = stringBuilder1.toString().length();
		int stringLen1 = stringBuilder2.toString().length();
		FileOutputStream fileOutputStream = new FileOutputStream(new File("shortQuery.txt"));
		fileOutputStream.write(stringBuilder1.toString().getBytes(), 0, stringLen);
        fileOutputStream.close();
        FileOutputStream fileOutputStream1 = new FileOutputStream(new File("longQuery.txt"));
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