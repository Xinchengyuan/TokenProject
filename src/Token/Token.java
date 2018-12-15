package Token;

import java.util.Comparator;
import java.util.Objects;

public class Token {
	 private String content;
	 private int position;
	 private int did;
	 private double tf;
	 private int df;
	 private Double tfidf;
	 private String tid; //token id
	 public Token(String content, int did) {
		this.content = content;
		this.position=0;
		this.did=did;
		this.tf=0;
		this.df=0;
		this.tfidf=0.0;
	 } 
	 
	 public String getContent() {
		return content;
	 }
	 public void setPos(int pos) {
		 this.position=pos;
	 }
	 public int getPos() {
		 return position;
	 }
	 public void setId() { //assign an Id to the token containing its position and did
		                  //e.g. an id 371 means the token is at position 37 in doc.1
		 tid=Integer.toString(position)+ Integer.toString(did);
	
	 }
	 public String getId() {
		 return tid;
	 }
	 public double getTf() {
 		return tf;
	 }
     public void setTf(double tf) {
		this.tf = tf;
	 }
	 public int getDf() {
		return df;
	 }
	 public void setDf(int df) {
		this.df = df;
	 }
	 public Double getTfidf() {
		return tfidf;
	 }
	 public void setTfidf(Double tfidf) {
		this.tfidf = tfidf;
	 }
	 public int getDid() {
		return did;
	 }
	 public void stemToken() { //stem the token
		PorterStemmer ps = new PorterStemmer();
		content= ps.stem(content);
     }	
	 @Override
     public boolean equals(Object o) {
	    if (o == this) return true;
	    if (!(o instanceof Token)) {
	       return false;
	    }
	    Token tk = (Token) o;
	       return did==tk.did && Objects.equals(content, tk.content);
	       //treat two tokens same if they are from same document and have identical content
	    }

	 @Override
	 public int hashCode() {
	        return Objects.hash(did,content);
	    }
}

class TokenComparator implements Comparator<Token> {
	//override the compare method in Comparator interface
		 public int compare(Token t, Token t1) {
			if(t.getDid()==t1.getDid()) {
				return t.getContent().compareTo(t1.getContent());
			}
			return t.getDid()-t1.getDid();
		 }
}
