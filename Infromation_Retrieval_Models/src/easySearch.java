
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

public class easySearch {
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

	public static void main(String[] args) throws ParseException {
	try {
		Scanner src = new Scanner(System.in);
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("C:\\Users\\AVIATOR\\Desktop\\IU\\Sem1\\SEARCH\\Assignment\\Assignment 2\\index")));
		IndexSearcher searcher = new IndexSearcher(reader);
		// Get the preprocessed query terms
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		//the total number of documents in the corpus
		int N = reader.maxDoc();
		System.out.println("Enter the Search query");
		String queryString = src.nextLine();
		Map<String, Float> relevantDoc = new HashMap<String, Float>();
		HashMap<String, Float> docLen = new HashMap<String, Float>();
		Query query = parser.parse(queryString);
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
		//Use DefaultSimilarity.decodeNormValue(…) to decode normalized document length
		ClassicSimilarity dSimi = new ClassicSimilarity();
		// Get the segments of the index
		List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
		// Processing each segment
		for (int i = 0; i < leafContexts.size(); i++) {
			// Get document length
			LeafReaderContext leafContext = leafContexts.get(i);
			int startDocNo = leafContext.docBase;
			int numberOfDoc = leafContext.reader().maxDoc();
			for (int docId = 0; docId < numberOfDoc; docId++) {
				// Get normalized length (1/sqrt(numOfTokens)) of the document
				float normDocLeng = dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));
				// Get length of the document
				float docLeng = 1 / (normDocLeng * normDocLeng);
				docLen.put(searcher.doc(docId+startDocNo).get("DOCNO"), docLeng);
			}	
			for (Term t : queryTerms) {
				int df=reader.docFreq(new Term("TEXT", t.text()));
				float idf = (float) Math.log(1+(N/(float)df));
				PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),"TEXT", new BytesRef(t.text()));
				if (de != null) {
					while (( de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
						float weight =  (float) ((de.freq()/docLen.get(searcher.doc(de.docID() +startDocNo).get("DOCNO")))*idf);
						relevantDoc.put(searcher.doc(de.docID() +startDocNo).get("DOCNO"), relevantDoc.containsKey(searcher.doc(de.docID() +startDocNo).get("DOCNO")) ? relevantDoc.get(searcher.doc(de.docID() +startDocNo).get("DOCNO")) + weight : weight);														
					}
				}
			}
		}		
		// Call to the sort function
		List<Entry<String, Float>> sortedRelevantDoc = entriesSortedByValues(relevantDoc);
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("DocID"+"          "+"Score");
		stringBuilder.append(System.getProperty("line.separator"));
		for (int i=0; i<sortedRelevantDoc.size();i++) {
			stringBuilder.append(sortedRelevantDoc.get(i).getKey()+"  "+sortedRelevantDoc.get(i).getValue());
			stringBuilder.append(System.getProperty("line.separator"));
		}
		int stringLen = stringBuilder.toString().length();
		FileOutputStream fileOutputStream = new FileOutputStream(new File("easySearch.txt"));
		fileOutputStream.write(stringBuilder.toString().getBytes(), 0, stringLen);
        fileOutputStream.close();
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