// Z534 Search (Information Retrieval)
// Author: Akshay Naik

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
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
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class generateIndex {
	
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
	
	public static void main(String[] args) {
		
		String sourceDir = "C:\\Users\\AVIATOR\\Desktop\\IU\\Sem1\\SEARCH\\Assignment\\Assignment 1\\corpus";
		String indexDir = "C:\\Users\\AVIATOR\\Desktop\\IU\\Sem1\\SEARCH\\Assignment\\Assignment 1\\IndexedDir folder\\StandardAnalyzer_indexedDir";
	try {
		//Indexes will be present in this Directory
		Directory dir = FSDirectory.open(Paths.get(indexDir));
		Analyzer analyzer = new StandardAnalyzer(); // We can use different analyzers to index our corpus just by changing this statement
		//Analyzer analyzer = new StopAnalyzer();
		//Analyzer analyzer = new SimpleAnalyzer();
		//Analyzer analyzer = new KeywordAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		//Indexer will be created to write the index directory 
		IndexWriter writer = new IndexWriter(dir, iwc);
		// Creating list of files from the input directory
		File[] files =new File(sourceDir).listFiles();
		for(File f: files) {
			    FileReader fileReader= new FileReader(f);
				String textFile = "";
				StringBuilder stringBuilder = new StringBuilder();
				String line = "";
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line);
				}
				bufferedReader.close();
				//stored content of file in string variable
				textFile = stringBuilder.toString();
				// Divided the file content by the <DOC> tag and created a string array wherein each element contain one <DCO></DOC> tag  
				String[] doclist = textFile.split("<DOC>");		
				// Create indexed documents, add fields to the document and add the document to the index directory  
				for(int i=1; i <doclist.length; i++) {
					String temp = doclist[i];
					Document doc = new Document();
					
					String matchedStr = getString(temp,"<DOCNO>(,*?)</DOCNO>");
					if (matchedStr != null)
						doc.add(new StringField("DOCNO", matchedStr ,Field.Store.YES));
					
					matchedStr = getString(temp,"<HEAD>(.*?)</HEAD>");
					if (matchedStr != null)
						doc.add(new TextField("HEAD", matchedStr ,Field.Store.YES));
					
					matchedStr = getString(temp,"<BYLINE>(.*?)</BYLINE>");
					if (matchedStr != null)
						doc.add(new TextField("BYLINE", matchedStr ,Field.Store.YES));
					
					matchedStr = getString(temp,"<DATELINE>(.*?)</DATELINE>");
					if (matchedStr != null)
						doc.add(new TextField("DATELINE", matchedStr ,Field.Store.YES));
					
					matchedStr = getString(temp,"<TEXT>(.*?)</TEXT>");
					if (matchedStr != null)
						doc.add(new TextField("TEXT", matchedStr ,Field.Store.YES));
					
			       	    
		       	  writer.addDocument(doc);
				 }
			}
			writer.forceMerge(1);
			writer.commit();
			writer.close();
			
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
