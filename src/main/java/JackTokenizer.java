import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class JackTokenizer {
	
	private BufferedReader bufferedReader = null;
	private String currentToken;
	private FileReader fileReader;
	private StreamTokenizer scanner;
	private String currentLine;
	private String nextLine;
	private Pattern pattern;
	private int tempToken;
	private String[] keywords= {"class","constructor","function","method","field", "static", "var","int","char","boolean",
			"void","true","false","null","this","let","do","if","else","return"};
	private String[] symbols = {"{","}","(",")","[","]",".",",",",",";","+","-","*","/","&","|","<",">","=","~"};
	
	public enum tokenType {KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST};
	public enum keyWord  {CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD,
		LET, DO, IF, ELSE, WHILE, RETURN, TRUE, FALSE, NULL, THIS};
	public HashMap<String,keyWord> keyWordMap = new HashMap<String,keyWord>();
JackTokenizer()	{
		this.pattern = Pattern.compile("(([a-zA-Z_0-9]+)\\(|\\)|\\{|\\}|;|\\=|\\+|\\*|\\-|\\.|,|\\/|\\&|\\||\\<|\\>|\\=|\\~)");
		System.out.println(this.pattern.pattern());
	keyWordMap.put("class",       keyWord.CLASS);
	keyWordMap.put("method",      keyWord.METHOD);
	keyWordMap.put("function",    keyWord.FUNCTION);
	keyWordMap.put("constructor", keyWord.CONSTRUCTOR);
	keyWordMap.put("int",         keyWord.INT);
	keyWordMap.put("boolean",     keyWord.BOOLEAN);
	keyWordMap.put("char",        keyWord.CHAR);
	keyWordMap.put("void",        keyWord.VOID);
	keyWordMap.put("var",         keyWord.VAR);
	keyWordMap.put("static",      keyWord.STATIC);
	keyWordMap.put("field",       keyWord.FIELD);
	keyWordMap.put("let",         keyWord.LET);
	keyWordMap.put("do",          keyWord.DO);
	keyWordMap.put("let",         keyWord.LET);
	keyWordMap.put("if",          keyWord.IF);
	keyWordMap.put("else",        keyWord.ELSE);
	keyWordMap.put("while",       keyWord.WHILE);
	keyWordMap.put("return",      keyWord.RETURN);
	keyWordMap.put("true",        keyWord.TRUE);
	keyWordMap.put("false",       keyWord.FALSE);
	keyWordMap.put("null",        keyWord.NULL);
	keyWordMap.put("this",        keyWord.THIS);
	
}

public void setInputFile(File inputFile) {

	try {
		this.bufferedReader = new BufferedReader(new FileReader(inputFile));
		this.scanner = new StreamTokenizer(this.bufferedReader);
		this.scanner.ordinaryChar('(');
		this.scanner.ordinaryChar(')');
		this.scanner.ordinaryChar('{');
		this.scanner.ordinaryChar('}');
		this.scanner.ordinaryChar(';');
		this.scanner.ordinaryChar('=');
		this.scanner.ordinaryChar('+');
		this.scanner.ordinaryChar('*');
		this.scanner.ordinaryChar('-');
		this.scanner.ordinaryChar('.');
		this.scanner.ordinaryChar(',');
		//this.scanner.ordinaryChar('/');
		this.scanner.ordinaryChar('&');
		this.scanner.ordinaryChar('|');;
		this.scanner.ordinaryChar('<');
		this.scanner.ordinaryChar('>');
		this.scanner.ordinaryChar('~');
		
		
	}catch(IOException ex) {
		ex.printStackTrace();
	}
}

public boolean hasMoreTokens() throws IOException {
	mark();
	this.tempToken = this.scanner.nextToken();
	return this.tempToken != StreamTokenizer.TT_EOF;
}


public void advance() throws IOException {
	if(hasMoreTokens()) {
		setToken();
	System.out.println(this.currentToken);
	}
	
}

private void setToken() {
	
	if(this.scanner.ttype == StreamTokenizer.TT_NUMBER) {
		this.currentToken = Double.toString(this.scanner.nval);
	}
	else if(this.scanner.ttype == StreamTokenizer.TT_WORD) {
		this.currentToken = this.scanner.sval;
	}else {
		this.currentToken = String.valueOf((char)this.tempToken);
	}
}
public void pushBack() throws IOException {
	this.bufferedReader.reset();
	setToken();
}
public void mark() throws IOException {
	this.bufferedReader.mark(1);
}
public boolean readNextLine() throws IOException {
	String tempLine = this.bufferedReader.readLine().replaceAll("//.*$", "").replaceAll("/\\*\\*.*\\*/", "");
	if(tempLine.startsWith("/**")) {
		while(!tempLine.endsWith("*/") || tempLine.startsWith("/**")) {
			tempLine = this.bufferedReader.readLine();
		}

	}
	if(tempLine.equals("")) {
		return readNextLine();
	}
	
	this.currentLine = tempLine;
	
	return this.currentLine == null;
	
}

public tokenType tokenType() {
	if(keyWordMap.containsKey(this.currentToken)) {
		return tokenType.KEYWORD;
	}
	if(Arrays.asList(symbols).contains(this.currentToken)) {
		return tokenType.SYMBOL;
	}
	
	if(StringUtils.isNumeric(this.currentToken)) {
		return tokenType.INT_CONST;
	}
	if(this.currentToken.startsWith("\"")&& this.currentToken.endsWith("\"")) {
		String temp = this.currentToken.substring(1, this.currentToken.length()-1);
		if(!temp.contains("\"") && !temp.contains("\n")) {
			this.currentToken = temp;
			return tokenType.STRING_CONST;
		}

	}

	return tokenType.IDENTIFIER; //really?
	
}

public keyWord keyWord() {
	String keyWord = this.currentToken.toLowerCase();
	return keyWordMap.get(keyWord);
}

public char symbol() {
	return this.currentToken.charAt(0);
}
public String identifier() {
	return this.currentToken;
}
	
public int intVal() {
	return Integer.parseInt(this.currentToken);
}
 public String stringVal() {
	 return this.currentToken;
	 
 }

 public String getCurrentToken() {
	 return this.currentToken;
 }

}
