# TokenProject
This project consists of :
-PorterStemmer+Stemmer(interface): a stemmer adopted from online source. 
               The api can be found at https://opennlp.apache.org/docs/1.7.2/apidocs/opennlp-tools/opennlp/tools/stemmer/PorterStemmer.html.
-Preprocessor: a tool to divide every paragraph of a file into sub-files, delimited by period. User is required to enter the directory of
               files to process and directory to save the output files to.
-Token: a token class featuring a token object
-Tokenizer: a tool with the following functions:
            1) split file(s) from user specified directory into tokens
            2) Calculate TFIDF of each token, determine the gap of TFIDF. and find keywords from Files
            3) Load tokens to user specified database, and create a byte table into the database,which maps tokens with TFIDF above gap to
               value 1, and 0 otherwise
               
Environment Requirement:
- Java JRE
- MySQL server connection via apps such as XAMPP
