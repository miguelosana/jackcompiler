import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Pattern;


public class JackTokenizer {
	
	private BufferedReader bufferedReader = null;
	private String currentToken;
	private FileReader fileReader;
	private StreamTokenizer scanner;
	private String currentLine;
	private String nextLine;
	private Pattern pattern;
	private ArrayList<String> tokenStack = new ArrayList<String>();
	private int tempToken;
	private int pointer;
	private String[] keywords= {"class","constructor","function","method","field", "static", "var","int","char","boolean",
			"void","true","false","null","this","let","do","if","else","return"};
	private String[] symbols = {"{","}","(",")","[","]",".",",",",",";","+","-","*","/","&","|","<",">","=","~"};
	
	public enum tokenType {KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST};
	public enum keyWord  {CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD,
		LET, DO, IF, ELSE, WHILE, RETURN, TRUE, FALSE, NULL, THIS};
	public HashMap<String,keyWord> keyWordMap = new HashMap<String,keyWord>();
JackTokenizer()	{
		this.pattern = Pattern.compile("(([ws]+)|([a-zA-Z_0-9]+)|\\(|\\)|\\{|\\}|;|\\=|\\+|\\*|\\-|\\.|,|\\/|\\&|\\||\\<|\\>|\\=|\\~)");
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

public void setInputFile(File inputFile) throws IOException {

	try {
		this.bufferedReader = new BufferedReader(new FileReader(inputFile));
		this.scanner = new StreamTokenizer(this.bufferedReader);
		this.scanner.slashSlashComments(true);
		this.scanner.slashStarComments(true);
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
		this.scanner.ordinaryChar('/');
		this.scanner.ordinaryChar('&');
		this.scanner.ordinaryChar('|');;
		this.scanner.ordinaryChar('<');
		this.scanner.ordinaryChar('>');

		this.scanner.ordinaryChar('~');
		this.scanner.quoteChar('"');
		
		
	}catch(IOException ex) {
		ex.printStackTrace();
	}
	
	this.pointer = 0;
	this.tokenStack.clear();
	while((this.tempToken = this.scanner.nextToken()) != StreamTokenizer.TT_EOF) {
			setToken();
	}
}

public boolean hasMoreTokens() throws IOException {
	return pointer < this.tokenStack.size();
}


public void advance() throws IOException {
	if(hasMoreTokens()) {
		this.currentToken = this.tokenStack.get(this.pointer);
		this.pointer++;
	System.out.println(this.currentToken);
	}
	
}

private void setToken() {
	
	if(this.scanner.ttype == StreamTokenizer.TT_NUMBER) {
		Double tmp = this.scanner.nval;
		this.tokenStack.add(Integer.toString(tmp.intValue()));
	}
	else if(this.scanner.ttype == StreamTokenizer.TT_WORD) {
		this.tokenStack.add(this.scanner.sval);
	}else {
		if(this.tempToken == '"') {
			this.tokenStack.add("string-constant:"+this.scanner.sval);
		}else {
		this.tokenStack.add(String.valueOf((char)this.tempToken));
		}
	}
}
public void pushBack() throws IOException {
	System.out.println(this.currentToken);
	System.out.println(this.pointer);

	this.pointer--;
	this.currentToken = this.tokenStack.get(this.pointer);
	System.out.println(this.pointer);
	System.out.println(this.currentToken);
}
public boolean readNextLine() throws IOException {
	String tempLine = this.bufferedReader.readLine().replaceAll("//.*$", "").replaceAll("/\\*\\*.*\\*/", "");
	if(tempLine.startsWith("/**")) {
		boolean foundEnd = tempLine.endsWith("*/");
		while(!foundEnd) {
			tempLine = this.bufferedReader.readLine();
			foundEnd = (tempLine.endsWith("*/") || tempLine.startsWith("*/"));
		}

	}
	if(tempLine.equals("")) {
		return readNextLine();
	}
	
	this.currentLine = tempLine;
	
	return this.currentLine == null;
	
}

public tokenType tokenType() throws IOException {
	if(keyWordMap.containsKey(this.currentToken)) {
		return tokenType.KEYWORD;
	}
	if(Arrays.asList(symbols).contains(this.currentToken)) {
		return tokenType.SYMBOL;
	}
	
	if(isNumeric(this.currentToken)) {
		return tokenType.INT_CONST;
	}
	if(this.currentToken.startsWith("string-constant:")) {
		this.currentToken.replace("string-constant:", "");
			return tokenType.STRING_CONST;
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
 public static boolean isNumeric(final CharSequence cs) {
     if ((cs == null || cs.length() == 0)) {
         return false;
     }
     final int sz = cs.length();
     for (int i = 0; i < sz; i++) {
         if (!Character.isDigit(cs.charAt(i))) {
             return false;
         }
     }
     return true;
 }
 public String getCurrentToken() {
	 return this.currentToken;
 }
public boolean isOp() {
	boolean result = false;
	switch(this.currentToken) {
	case "+":
	case "-":
	case "*":
	case "/":
	case "&":
	case "|":
	case "<":
	case ">":
	case "=":
		result =true;
		break;
		
		
	}
	return result;
}
}
