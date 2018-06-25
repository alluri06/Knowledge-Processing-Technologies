import java.util.*;
import java.io.*;

public class QueryProcessing {

	String[] myDocs;
	private static String[] stopList ;
	public ArrayList<String> termList;
	public ArrayList<Integer> termFrequency;
	public ArrayList<ArrayList<Integer>> docLists;


	public QueryProcessing(String folderName) {

		termList = new ArrayList<String>();
		termFrequency = new ArrayList<Integer>();
		docLists = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> docList;

		// load files into myDocs
		File folder = new File(folderName);
		File[] listOfFiles = folder.listFiles();
		myDocs = new String[listOfFiles.length];
		for (int i = 0; i < listOfFiles.length; i++) {
			myDocs[i] = listOfFiles[i].getName();
		}

		//load stopwords and sort
		stopList = loadStopwords("stopwords.txt");
		Arrays.sort(stopList);

		for (int i = 0; i < myDocs.length; i++) {
			String[] tokens = parse(folderName + "/" + myDocs[i]);

			for (String token : tokens) {

				if (searchStopword(token) == -1 && token.length()!=1 && !token.isEmpty()){
					token = stemming(token);
					if (!termList.contains(token)) {//a new term
						termList.add(token);
						termFrequency.add(1);
						docList = new ArrayList<Integer>();
						docList.add(i);
						docLists.add(docList);
					} else {//an existing term
						int index = termList.indexOf(token);
						docList = docLists.get(index);

						if (!docList.contains(i)) {
							docList.add(i);
							docLists.set(index, docList);
							int tf = termFrequency.get(index);
							tf++;
							termFrequency.set(index, tf);
						}
					}
				}
			}
		}
	}

	public static String stemming(String token){
		Stemmer st = new Stemmer();
		st.add(token.toCharArray(), token.length());
		st.stem();
		return st.toString();
	}

	public String toString(){
		String matrixString = new String();
		ArrayList<Integer> docList;
		System.out.println("termList\tFrequency\tpostings");
		for(int i=0;i<termList.size();i++){
			matrixString += String.format("%-15s", termList.get(i));
			matrixString += Integer.toString(termFrequency.get(i))+"\t\t";
			docList = docLists.get(i);
			for(int j=0;j<docList.size();j++)
				matrixString += docList.get(j) + "\t";
			matrixString += "\n";
		}
		matrixString += "------------------------------";
		System.out.println("-----------------------------------");
		return matrixString;
	}
	
	public String[] parse(String fileName)
	{
		/*
		   Tokenization: Creates an array of tokens
		 */
		String[] tokens = null;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String allLines = new String();
			String line = null;
			while((line=reader.readLine())!=null){
				allLines += line.toLowerCase(); //case folding
			}

			tokens = allLines.split("[ .,&%$#!/+()-*^?:\"--]+");
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		return tokens;
	}



	public static String[] loadStopwords(String stopwordsFile){

		String[] stopWords = null;
		String alllines= "";
		try{
			BufferedReader reader = new BufferedReader(new FileReader(stopwordsFile));
			String allLines = new String();
			String line = null;
			while((line=reader.readLine())!=null){
				allLines += line.toLowerCase()+"\n"; //case folding
			}
			stopWords = allLines.split("\n");
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		return stopWords;
	}
	
	public static int searchStopword(String key)
	{
		/*
		  Search if key is present in the stopwordList using binarySearch
		  (stopwordList should be sorted)
		 */
		int lo = 0;
		int hi = stopList.length-1;
		while(lo<=hi)
		{
			//Key is in a[lo..hi] or not present
			int mid = lo + (hi-lo)/2;
			int result = key.compareTo(stopList[mid]);
			if(result <0) hi = mid - 1;
			else if(result >0) lo = mid+1;
			else return mid;
		}
		return -1;
	}


	public ArrayList<Integer> search(String query) {
		query= stemming(query);
		query.toLowerCase();
		int index = termList.indexOf(query);
		//System.out.println(query+ " , "+ index);
		if (index < 0)
			return null;
		return docLists.get(index);
	}

	public ArrayList<Integer> andSearch(String[] query) {
		ArrayList<Integer> result = search(query[0]);
		int termId = 1;
		while (termId < query.length) {
			ArrayList<Integer> result1 = search(query[termId]);
			result = merge(result, result1);
			termId++;
		}

		return result;
	}

	public ArrayList<Integer> multiSearch(String[] query) {

		ArrayList<Node> listNode = new ArrayList<>();
		for(String s:query) {
			int index = termList.indexOf(s);
			int tf = termFrequency.get(index);
			//System.out.println(s +","+ tf);
			Node node = new Node(tf, s);
			listNode.add(node);
			Collections.sort(listNode, (a, b) -> a.freq - b.freq);
		}

		System.out.println("Query processing order is: \n ");
		String start = (listNode.get(0)).word;
        Integer tf0 = ((listNode.get(0)).freq);
		ArrayList<Integer> result = search(start);
		System.out.println(1+"."+"\t"+start + "\t"+Integer.toString(tf0)+"\n");

		for(int k=1; k<listNode.size(); k++){
			String s= (listNode.get(k)).word;
			Integer tf = ((listNode.get(k)).freq);
			ArrayList<Integer> result1 = search(query[k]);
			result = merge(result, result1);
			System.out.println((k+1)+"."+"\t"+s+"\t"+ Integer.toString(tf)+ "\n");
		}

		return result;
	}

	public ArrayList<Integer> orSearch(String[] query) {
		ArrayList<Integer> result = search(query[0]);
		int termId = 1;
		while (termId < query.length) {
			//System.out.println(query[termId]);
			ArrayList<Integer> result1 = search(query[termId]);
			result = union(result, result1);
			termId++;
		}
		return result;
	}

	private ArrayList<Integer> union(ArrayList<Integer> l1, ArrayList<Integer> l2) {
		ArrayList<Integer> mergedList = new ArrayList<Integer>();
		int id1 = 0, id2 = 0;
		while (id1 < l1.size() && id2 < l2.size()) {
			if (l1.get(id1).intValue() == l2.get(id2).intValue()) {
				mergedList.add(l1.get(id1));
				id1++;
				id2++;
			} else if (l1.get(id1) < l2.get(id2)) {
				mergedList.add(l1.get(id1));
				id1++;
			} else {
				mergedList.add(l2.get(id2));
				id2++;
			}
		}

		while (id1 < l1.size()) {
			mergedList.add(l1.get(id1));
			id1++;
		}

		while (id2 < l2.size()) {
			mergedList.add(l2.get(id2));
			id2++;
		}

		return mergedList;
	}

	private ArrayList<Integer> merge(ArrayList<Integer> l1, ArrayList<Integer> l2){
		ArrayList<Integer> mergedList = new ArrayList<Integer>();

		int id1 = 0, id2 = 0;
		if(l1.size()==0) {
			return l2;
		}
		if(l2.size()==0){
			return l1;
		}
		if(l1.size()==0 && l2.size()==0){
			System.out.println("Cannot Merge");
			System.exit(0);
		}
		while (id1 < l1.size() && id2 < l2.size()) {
			if (l1.get(id1).intValue() == l2.get(id2).intValue()) {
				mergedList.add(l1.get(id1));
				id1++;
				id2++;
			} else if (l1.get(id1) < l2.get(id2))
				id1++;
			else
				id2++;
		}
		return mergedList;
	}

	public static void main(String[] args) {

		ArrayList<Integer> result;
		ArrayList<Integer> result2;

		//Scanner sc = new Scanner(System.in);
		//System.out.println("Enter the folder name: \n");
		//String folderName = sc.nextLine();
		String folderName = "Lab1_Data/";

		//String folderName = "Lab1_Data/";
		QueryProcessing qp = new QueryProcessing(folderName);
		System.out.println(qp);

		// process query input
		Scanner sc2 = new Scanner(System.in);
		System.out.println("Enter the search query and 'exit' when done: ");
		String searchQuery ;
		while(!(searchQuery= sc2.nextLine()).equals("exit")){

			searchQuery=searchQuery.toLowerCase();
			String[] keywords = searchQuery.split(" ");

			if(keywords.length==1)
				result = qp.search(keywords[0]);

			else if(keywords.length==2) {
				result = qp.andSearch(keywords);

				result2 = qp.orSearch(keywords);
				if(result2.size()!=0){
					System.out.println("Query terms connected by OR ");
					for(Integer i:result2)
						System.out.println("\t"+"Document: "+ qp.myDocs[i]+ " , "+ "ID: "+i);
				}
				else
					System.out.println("No match(OR query)!");
			}
			else {
				result = qp.multiSearch(keywords);
			}

			if(result!=null){
				System.out.println("Simple Query/ Query terms connected by AND: ");
				for(Integer i:result)
					System.out.println("\t"+"Document: "+ qp.myDocs[i]+ " , "+ "ID: "+i);
			}
			else
				System.out.println("No match!");
			System.out.println("------------------------------");
			System.out.println("Enter the search query again: ");
			}

		System.out.println("*****Exit*****");
		System.exit(1);
	}
}