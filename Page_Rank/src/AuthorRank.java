
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class AuthorRank {
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
	
	public static void main(String[] args) {
		
		try {
			String fileName = "C:\\Users\\AVIATOR\\Desktop\\IU\\Sem1\\SEARCH\\Assignment\\Assignment 3\\author.net";
			File files =new File(fileName);
			FileReader fileReader= new FileReader(files);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			// Creates a directed graph 
			DirectedSparseGraph<String, String> graph = new DirectedSparseGraph<String, String>();
			Map<String, String> vertices = new HashMap<String, String>();
			Map<String, String> edges = new HashMap<String, String>();
			String line = "";
			line = bufferedReader.readLine();
			String[] splittedLine = line.split("\\s+");
			int numberOfVertices = Integer.parseInt(splittedLine[1]);
			for(int i = 0; i <numberOfVertices; i++) {
				line = bufferedReader.readLine();
				splittedLine = line.split("\\s+");
				vertices.put(splittedLine[0], splittedLine[1].substring(1, splittedLine[1].length()-1));
				// Adding vertices to graph
				graph.addVertex(splittedLine[1].substring(1, splittedLine[1].length()-1));
			}
			line = bufferedReader.readLine();
			splittedLine = line.split("\\s+");
			int numberOfEdges = Integer.parseInt(splittedLine[1]);
			for(int i = 0; i <numberOfEdges; i++) {
				line = bufferedReader.readLine();
				splittedLine = line.split("\\s+");
				edges.put(vertices.get(splittedLine[0]), vertices.get(splittedLine[1]));
				// Adding edges to graph
				graph.addEdge(Integer.toString(i),new Pair<String>(vertices.get(splittedLine[0]), vertices.get(splittedLine[1])),EdgeType.DIRECTED);
			}
			bufferedReader.close();
			// Creating a page rank object 
			PageRank<String, String> ranker = new PageRank<String, String>(graph, 0.15);
			// Assigning ranking score to vertices (i.e. Authors)
			ranker.evaluate();
			Map<String, Double> rankResult = new HashMap<String, Double>();
			for (String v : graph.getVertices()) {
				rankResult.put(v, ranker.getVertexScore(v));
			}
			// Sorting the vertices based on ranking score.
			List<Entry<String, Double>> sortedRelevantDoc = entriesSortedByValues(rankResult);
			// Displaying top10 authors based on ranking score
			System.out.println("Author ID"+"   "+"Page Rank Score");
			for (int i=0; i<10; i++) {
				System.out.println(sortedRelevantDoc.get(i).getKey()+"         "+sortedRelevantDoc.get(i).getValue()+ "   ");
			}
							
		}
			
		catch(Exception e){
			e.printStackTrace();
		}

	}

}
