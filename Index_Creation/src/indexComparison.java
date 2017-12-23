// Z534 Search (Information Retrieval)
// Author: Akshay Naik

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class indexComparison {

	public static void main(String[] args) {
	try {	
		  String indexDir = "C:\\Users\\AVIATOR\\Desktop\\IU\\Sem1\\SEARCH\\Assignment\\Assignment 1\\IndexedDir folder\\StandardAnalyzer_indexedDir";
//		  String indexDir = "C:\\Users\\AVIATOR\\Desktop\\IU\\Sem1\\SEARCH\\Assignment\\Assignment 1\\IndexedDir folder\\StopAnalyzer_indexedDir";
//		  String indexDir = "C:\\Users\\AVIATOR\\Desktop\\IU\\Sem1\\SEARCH\\Assignment\\Assignment 1\\IndexedDir folder\\KeywordAnalyzer_indexedDir";
//		  String indexDir = "C:\\Users\\AVIATOR\\Desktop\\IU\\Sem1\\SEARCH\\Assignment\\Assignment 1\\IndexedDir folder\\SimpleAnalyzer_indexedDir";
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get( (indexDir))));
		  //Print the total number of documents in the corpus
		  System.out.println("Total number of documents in the corpus: "+reader.maxDoc());                            
          //Print the number of documents containing the term "new" in <field>TEXT</field>.
          System.out.println("Number of documents containing the term \"new\" for field \"TEXT\": "+reader.docFreq(new Term("TEXT", "new")));
	      //Print the total number of occurrences of the term "new" across all documents for <field>TEXT</field>.
          System.out.println("Number of occurrences of \"new\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","new")));                                                       

          Terms vocabulary = MultiFields.getTerms(reader, "TEXT");

          //Print the size of the vocabulary for <field>TEXT</field>, applicable when the index has only one segment.
          System.out.println("Size of the vocabulary for this field: "+vocabulary.size());
          //Print the total number of documents that have at least one term for <field>TEXT</field>
          System.out.println("Number of documents that have at least one term for this field: "+vocabulary.getDocCount());
          //Print the total number of tokens for <field>TEXT</field>
          System.out.println("Number of tokens for this field: "+vocabulary.getSumTotalTermFreq());
          //Print the total number of postings for <field>TEXT</field>
          System.out.println("Number of postings for this field: "+vocabulary.getSumDocFreq());      
         //Print the vocabulary for <field>TEXT</field>
          TermsEnum iterator = vocabulary.iterator();
        
          BytesRef byteRef = null;
          //Print the output in text file
          StringBuilder stringBuilder = new StringBuilder();
          stringBuilder.append("Total number of documents in the corpus: "+reader.maxDoc());
          stringBuilder.append(System.getProperty("line.separator"));
          stringBuilder.append("Number of documents containing the term \"new\" for field \"TEXT\": "+reader.docFreq(new Term("TEXT", "new")));
          stringBuilder.append(System.getProperty("line.separator"));
          stringBuilder.append("Number of occurrences of \"new\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","new")));
          stringBuilder.append(System.getProperty("line.separator"));
          stringBuilder.append("Size of the vocabulary for this field: "+vocabulary.size());
          stringBuilder.append(System.getProperty("line.separator"));
          stringBuilder.append("Number of documents that have at least one term for this field: "+vocabulary.getDocCount());
          stringBuilder.append(System.getProperty("line.separator"));
          stringBuilder.append("Number of tokens for this field: "+vocabulary.getSumTotalTermFreq());
          stringBuilder.append(System.getProperty("line.separator"));
          stringBuilder.append("Number of postings for this field: "+vocabulary.getSumDocFreq());
          stringBuilder.append(System.getProperty("line.separator"));
          stringBuilder.append("\n*******Vocabulary-Start**********");
          stringBuilder.append(System.getProperty("line.separator"));
          


		  while((byteRef = iterator.next()) != null) {
		  	 String term = byteRef.utf8ToString();
	         stringBuilder.append(term + "\t");
		 }
		  stringBuilder.append("\n*******Vocabulary-End**********");
		  stringBuilder.append(System.getProperty("line.separator"));      
		  reader.close();
		  int stringLen = stringBuilder.toString().length();
		  FileOutputStream fileOutputStream = new FileOutputStream(new File("C:\\Users\\AVIATOR\\Desktop\\IU\\Sem1\\SEARCH\\Assignment\\Assignment 1\\Outputs\\StandardAnalyser.txt"));
//        FileOutputStream fileOutputStream = new FileOutputStream(new File("C:\\Users\\AVIATOR\\Desktop\\IU\\Sem1\\SEARCH\\Assignment\\Assignment 1\\Outputs\\StopAnalyser.txt"));
//        FileOutputStream fileOutputStream = new FileOutputStream(new File("C:\\Users\\AVIATOR\\Desktop\\IU\\Sem1\\SEARCH\\Assignment\\Assignment 1\\Outputs\\KeywordAnalyser.txt"));
//        FileOutputStream fileOutputStream = new FileOutputStream(new File("C:\\Users\\AVIATOR\\Desktop\\IU\\Sem1\\SEARCH\\Assignment\\Assignment 1\\Outputs\\SimpleAnalyser.txt"));
         fileOutputStream.write(stringBuilder.toString().getBytes(), 0, stringLen);
         fileOutputStream.close();
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
