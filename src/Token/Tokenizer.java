package Token;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;


public class Tokenizer {
  File[] fs;
  List<Token> words; //containing only tokens consists of alphabetic letters
  List<Token> uniq; //containing unique tokens of each file
  Map<Token, Double> ttf; // a hashmap mapping each token to its tf
  Map<String, Integer> tdf; // a hashmap mapping each token content to its df
                           // since to find tdf we are just interested in if the same token content appeared in other documents
  Map<Token, Integer> rst; //a hashmap mapping each token to a value 0 if it's tfidf is above the gap and 1 otherwise
  Set<String> uniqTks; //contains unique words from all documents 
  int [] docLengths; // stores total number of tokens in each documents
  private boolean[] ascii_set;
  private Connection conn;
  private Connection conn1;
  String db; //database name
  
  public Tokenizer(File[] fs) {
	this.fs = fs;
	words= new ArrayList<Token>();
	uniq= new ArrayList<Token>();
	docLengths=new int[fs.length];
	ttf= new HashMap<>();
	tdf= new HashMap<>();
	rst= new HashMap<>();
	uniqTks= new HashSet<>();
	this.ascii_set= new boolean[256];
	for(int i=0;i<ascii_set.length;i++) {
		ascii_set[i]= false;
	}	
	String letters= "abcdefghijklmnopqrstuvwxyzABCDEFGHIKLMNOPQRSTVXYZ";
	//populate ascii_set
	for (int j=0; j<letters.length();j++) {
	   int alph = letters.charAt(j);
	   ascii_set[alph]=true;
	}
  }
  
  //helper functions
  public boolean isAletter(char ch) {  //check if the input string is an alphabetic letter	 	
	  if(ch>255) {
	 	 return false;
	  }
	  else {
	 	 if(!ascii_set[ch]) return false; 
	  }
	  return true;
   } 
	  
  /**
   * split a set of files into an arrayList of tokens and give each one a token id
   */
  public void Tokenize(){
	 try {
	  int pos=0; //token position
	  int did=1; //document id
	  //int count=0; //track the number of tokens each document
	  for(File f:fs) {
		   //if(f.getName().endsWith(".txt")) {
			 Scanner in= new Scanner(f);
			 while(in.hasNextLine()) {
				 String proto = ""; //create an empty string to fill in letters to become a token later
				 String line= in.nextLine().trim().toLowerCase(); //trim leading and trailing spaces from line
				 for(int i=0; i<line.length();i++) {
					 //the following snippet records a word if we seen letters in between two non-letter characters
				   if(!isAletter(line.charAt(i))) {
					   if(!proto.equals("")) { //make sure we are not recording an empty proto string when non-letter is encountered
						  Token tk= new Token(proto, did);         // record the current proto string as a whole word when non-letter is encountered into a new token
						  tk.stemToken(); //stem the token
						  words.add(tk);
						  tk.setPos(pos); //pos here is still the position previously preserved at the starting of the word 
						  tk.setId();
						  pos++; //keep on track with position of the current non-letter character
					   }
					   proto= ""; //make a new proto string after recording for next use
					   pos++; //advance to the next position
				   }
				   else {
					   proto+= line.charAt(i);   
				   } 
				 }
		     } //end of while
					 docLengths[did-1]=pos;
					 pos=0; //reset position value
					 //count=0;
					 did++;
					 in.close();
	         //}
	       }
     } catch(FileNotFoundException e) {
    	 e.printStackTrace();
     }
  }
  
  
  /**
   * Calculate tfidf of a token
   * @param tk: the token we are interested in 
   * @return tfidf: the tfidf result
   */
  public double tfIdf(Token tk) {	  
	  return tk.getTf()*(Math.log((double)(fs.length/tk.getDf()))/Math.log(2));
  }
  /**
   * Calculate token frequency
   */
  public void tokenFreq() {
	 double tfTemp=0.0;
	 for(Token wd: words) {
			 if(!ttf.containsKey(wd)) { //the token is not in the hashmap
				ttf.put(wd, 1.0/docLengths[wd.getDid()-1]); // assign the new token a tf of 1 
			 }
			 else {
				   tfTemp= ttf.get(wd);
			       ttf.put(wd, tfTemp+1.0/docLengths[wd.getDid()-1]);
			 }
		 }
  }
  /**
   * Calculate document frequency
   */
  public void docFreq() {
	  int tdTemp = 0;
	 
	  for (Token tks:ttf.keySet()) {
		  uniqTks.add(tks.getContent());
	  }
	  for (String st:uniqTks) {
		   tdf.put(st, tdTemp);			  
	  }
	  for (String st:uniqTks) {
		  for(int i=1; i<=fs.length; i++) { //did starts from 1
			   if(ttf.containsKey(new Token(st, i))) {
				   tdTemp= tdf.get(st);
			       tdf.put(st, tdTemp+1);
			   }
		   } 
	  }
  }
  
  /**
   * Calculate tfidf of distinct tokens of each document
   */
  public void calcTfidf() {
	  Tokenize();
	  tokenFreq();
	  docFreq();
	  for(Token wds:ttf.keySet()) {
		  wds.setTf(ttf.get(wds));
		  wds.setDf(tdf.get(wds.getContent()));
		  wds.setTfidf(tfIdf(wds));
	  }	 
	  printTFidf();
  }
  
  public void printTFidf() {
	  for(int i=1; i<=fs.length;i++) {
	   System.out.println("============"+fs[i-1].getName()+"============");
	   System.out.format("%10s%12s%8s%9s%12s\n","TID","Token","TF", "DF","TFIDF");
	   for(Token wds:ttf.keySet()) {
	     if(wds.getDid()==i) {
	    	 System.out.format("%10s%12s%10.4f%10.5f%12.8f\n",wds.getId(),wds.getContent(),wds.getTf(), (fs.length*1.0/(wds.getDf()*1.0)),wds.getTfidf());
	     }
	   }
	  }
  }
  public void printKw() { // finds gap and prints out keywords
	  for(Token wds: ttf.keySet()) {
		  uniq.add(wds);
	  }
	  uniq.sort((Token tk1, Token tk2)->tk1.getTfidf().compareTo(tk2.getTfidf())); //sort tokens based on tfidf
	  double maxDif=0.0;
	  Token gapTkLft= null;
	  Token gapTkRht= null;
	  double gap;
	  for (int i=0;i<uniq.size()-1;i++) {
		  if((uniq.get(i+1).getTfidf()-uniq.get(i).getTfidf())>maxDif) {
			  maxDif=uniq.get(i+1).getTfidf()-uniq.get(i).getTfidf();
			  gapTkLft=uniq.get(i);
			  gapTkRht=uniq.get(i+1);
		  }
	  }
	  gap= (gapTkLft.getTfidf()+gapTkRht.getTfidf())/2.0;
	  System.out.printf("The gap of TFIDF:  %.8f\n", gap);
	  System.out.println("Keywords:");
	  System.out.format("%10s%10s\n","TID","Token");
	  for(Token wds:uniq) {
		  if(wds.getTfidf()>=gap) {
			  System.out.format("%10s%12s\n", wds.getId(), wds.getContent());
		      rst.put(wds, 1);
		  }
		  else {
			  rst.put(wds, 0);
		  }
	  }
  }
  
  public void loadRst (){ //load resulting binary hashmap by putting 0 if a token doesn't exist in some document
	  for (String st: uniqTks) {
	       for(int j=1; j<fs.length+1;j++) {
	    	   if(!uniq.contains(new Token(st,j))) {
	    		  rst.put(new Token(st,j),0);
	    	   }
  	    }
	  }

  }
  
  public void createDB() { //creates a database
	  Statement stmt=null;
	  try {
		Class.forName("com.mysql.cj.jdbc.Driver");
		String host="jdbc:mysql://localhost";
	    conn= DriverManager.getConnection(host, "root", "");
	    stmt= conn.createStatement();
	    Scanner scanner = new Scanner(System.in);
	    System.out.println("Type in the name of database to connect to");
	    db= scanner.next();
	    String sql = "CREATE DATABASE "+ db;
	    stmt.executeUpdate(sql);
	    scanner.close();
	} catch (SQLException | ClassNotFoundException err) {
		// TODO Auto-generated catch block
		System.out.println(err.getMessage());
	}
	  
  }
  
  public void loadDB() {
	  try {
		  createDB();
		  Class.forName("com.mysql.cj.jdbc.Driver");
		  Statement stmt1=null;
	      System.out.println("Connecting to the selected database...");
	      System.out.println("Connected to database!");
	      System.out.println("Creating table in given database...");
	      String dbhost="jdbc:mysql://localhost"+ "/"+db;
	      conn1= DriverManager.getConnection(dbhost, "root", "");
	      stmt1= conn1.createStatement();
	      String entry[] = new String[uniqTks.size()];
	      int index=0;
	      for(String st: uniqTks) {
	    	if(index<entry.length) {
	    	 entry[index]=st;
	    	 index++;
	    	}
	      }
	     
	      Arrays.sort(entry);
	      loadRst();
	  
	      String createquery = "CREATE TABLE SQLTOKEN ( " + "Document VARCHAR(15) ";

	      for (int i = 0; i < entry.length; i++) {
	              createquery += " , " + entry[i] +"_"+ " INT ";
	      }
	      createquery += ");";	   
          stmt1.executeUpdate(createquery);
	      System.out.println("Created table in given database...");
	      String alterquery= "ALTER TABLE SQLTOKEN " + 
	                         "ADD CONSTRAINT PK_SQLTOKEN " + 
	      		"PRIMARY KEY (Document);";
	      stmt1.executeUpdate(alterquery); //set Document as primary key
	      String prepare= "INSERT INTO SQLTOKEN VALUES (?";
	      for(int i=0;i<entry.length;i++) {
	    	  prepare+= ",?";
	      }
	      prepare+=")";
	     
	      for (int i=0; i<fs.length;i++) {
	    	  PreparedStatement insert=conn1.prepareStatement(prepare);
	    	  insert.setString(1, fs[i].getName());
	    	  for(int j=0; j<entry.length;j++) {
	    		  insert.setInt(j+2,rst.get(new Token(entry[j],i+1)));
	    	  }
	    	  insert.executeUpdate();
	      }
	      
	      //log the dfs of each token
	    	  PreparedStatement logdf=conn1.prepareStatement(prepare);
	    	  logdf.setString(1,"DF");
	    	  for(int i=0; i<entry.length;i++) {
	    		  logdf.setInt(i+2,tdf.get(entry[i]));
	    	  }
	    	  logdf.executeUpdate();
	      
	  }catch (SQLException | ClassNotFoundException err) {
		  System.out.println(err.getMessage());
	  }
  }
  
  public static void main (String[] args) throws FileNotFoundException {
	  Scanner sc = new Scanner(System.in);
	  System.out.println("Enter directory of folder containing the input files");
	  String dir= sc.next();
	  File[] tx= new File(dir).listFiles();
      Tokenizer tkn= new Tokenizer(tx);
	  long startTime = System.nanoTime();
	  tkn.calcTfidf();
	  tkn.printKw();
	  tkn.loadDB();
	  long endTime   = System.nanoTime();
	  long totalTime = endTime - startTime;
	  System.out.println("\n"+"Run Time: "+ totalTime/Math.pow(10, 9)+ "s");
	  sc.close();
	  
  }

}
