
import java.util.*;
import java.io.*;

public class Parser {
	String[] myDocs;
	ArrayList<String> termList;
	String[] stopList = {"a","is","in","so","of","at","the","to","and","it","as","be","are"};
	
	public Parser(String folderName)
	{
		File folder = new File(folderName);
		File[] listOfFiles = folder.listFiles();
		myDocs = new String[listOfFiles.length];
		for(int i=0;i<listOfFiles.length;i++)
		{
			System.out.println("File " + listOfFiles[i].getName());
			myDocs[i] = listOfFiles[i].getName();
		}
		String[] tokens = parse(folderName + "/" + myDocs[4]);
		Arrays.sort(stopList);
		for(String token:tokens){
			if(searchStopword(token)==-1)
				System.out.println(token);
		}
	}
	
	public String[] parse(String fileName)
	{
		String[] tokens = null;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String allLines = new String();
			String line = null;
			while((line=reader.readLine())!=null){
				allLines += line.toLowerCase(); //case folding
			}
			tokens = allLines.split("[ .,?!:;$%]+");
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		return tokens;
	}
	
	public int searchStopword(String key)
	{
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
	
	public static void main(String[] args)
	{
		Parser p = new Parser(args[0]);
		System.out.println("Stopwords: " + p.searchStopword("are"));
		Stemmer st = new Stemmer();
		st.add("cement".toCharArray(),"cement".length());
		st.stem();
		System.out.println("stemmed: " + st.toString());
	}

}
