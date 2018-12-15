package Token;

import java.io.*;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.regex.Pattern;


public class Preprocessor {
	public static void main(String args[]) {
        try {
        	 Scanner sc = new Scanner(System.in);
       	     System.out.println("Enter directory of folder containing the input files");
        	 String dir= sc.next();
        	 File[] tx= new File(dir).listFiles();
        	 System.out.println("Enter the directory where the output files will be saved");
        	 String target= sc.next();
       	     for(int i=0; i<tx.length;i++) {
     			String content = new String(Files.readAllBytes(tx[i].toPath()), "UTF-8");
     			String[] parag = content.trim().split(Pattern.quote(".")); 
     			for(int j=0; j<parag.length;j++) {
     			   FileWriter fw = new FileWriter(new File(target, "file"+i+"_"+ j+".txt")); 
     			   fw.write(parag[j]);
     			   fw.close(); 
     			}  		       
        	}
       	     sc.close();
           
        } catch(IOException e) {
            System.out.print("Exception");
        }
    }
}
