import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class CompilationEngine {

	private JackTokenizer tokenizer;
	private BufferedWriter bw = null;
	
	public CompilationEngine( JackTokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}
	
	public void setFileName(String outputFileName) {
		
		try {
			File outputFile = new File(outputFileName);
			FileOutputStream fos = new FileOutputStream(outputFile);
			this.bw = new BufferedWriter(new OutputStreamWriter(fos));
			System.out.println(outputFileName);

			
		}catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void compileClass() throws IOException,Exception {
		checkCurrentToken("class");
		writeStartTag("class", true);
		writeKeyword("class");
			writeIdentifier();
			writeSymbol("{");
				while(this.tokenizer.keyWord().equals(JackTokenizer.keyWord.STATIC) || this.tokenizer.keyWord().equals(JackTokenizer.keyWord.FIELD)) {
					compileClassVarDec();
				}
				while(this.tokenizer.tokenType().equals(JackTokenizer.tokenType.KEYWORD)) {
					compileSubroutine();
				}
			writeSymbol("}");
		
		writeEndTag("class");
		this.bw.close();
		}
		
	
	private void compileClassVarDec() throws IOException,Exception {
		writeStartTag("classVarDec",true);
			String keyword = this.tokenizer.getCurrentToken();
			writeKeyword(keyword);
			this.tokenizer.advance();
			String type = this.tokenizer.getCurrentToken();
			if(validType(type)) {
				writeType(type);
				//this.tokenizer.advance();
				writeIdentifier();
				if(this.tokenizer.getCurrentToken().equals(",")) {
					boolean hasMore = true;
					while(hasMore) {
						writeSymbol(",");
						writeIdentifier();
						hasMore = this.tokenizer.getCurrentToken().equals(",");
					}
					
				}
				if(this.tokenizer.getCurrentToken().equals(";")) {
					writeSymbol(";");
				}else {
					throw new Exception("Missing ; ");
				}
				
			}else {
				throw new Exception(String.format("%s is not a valid type for classVarDec", keyword));
			}
			
		
		writeEndTag("classVarDec");
		
	}
	private void compileSubroutine() throws IOException, Exception{
		if(this.tokenizer.tokenType().equals(JackTokenizer.tokenType.KEYWORD)) {
			String token = this.tokenizer.getCurrentToken();
			if(token.equals("constructor") || token.equals("function") || token.equals("method")) {
				writeStartTag("subroutineDec",true);
				writeKeyword(token);
				this.tokenizer.advance();
				String currentToken= this.tokenizer.getCurrentToken();
						
					if(currentToken.equals("void") || validType(currentToken)) {
						writeType(currentToken);
						writeIdentifier();
						writeSymbol("(");
						writeStartTag("parameterList", true);
						compileParameterList();
						writeEndTag("parameterList");
						writeSymbol(")");
						writeSubRoutineBody();
					}
				writeEndTag("subroutineDec");
			}
		}
	}
	
	
	private void compileParameterList() throws IOException, Exception{
		if(validType(this.tokenizer.getCurrentToken())) {
			writeType(this.tokenizer.getCurrentToken());
			writeIdentifier();
			//this.tokenizer.advance();
			boolean hasMore = this.tokenizer.getCurrentToken().equals(",");
			while(hasMore) {
				writeSymbol(",");
				if(validType(this.tokenizer.getCurrentToken()) && this.tokenizer.tokenType().equals(JackTokenizer.tokenType.KEYWORD)) {
					writeType(this.tokenizer.getCurrentToken());
				}
					writeIdentifier();
					hasMore = this.tokenizer.getCurrentToken().equals(",");
			}
		}

	}
	
	private void writeSubRoutineBody() throws IOException, Exception{
		writeStartTag("subroutineBody",true);
		writeSymbol("{");
			while(this.tokenizer.getCurrentToken().equals("var")) {
				compileVarDec();
			}
			compileStatements();
		
		writeSymbol("}");
		writeEndTag("subroutineBody");
	}
	
	private void compileStatements() throws IOException, Exception{
		writeStartTag("statements",true);
		boolean hasMore = true;
		while(hasMore) {
			switch(this.tokenizer.keyWord()) {
			case DO:
				compileDo();
				break;
			case LET:
				compileLet();
				break;
			case WHILE:
				compileWhile();
				break;
			case RETURN:
				compileReturn();
				break;
			case IF:
				compileIf();
				break;
			default:
				throw new Exception("Keyword is not a statement");
			}
			hasMore = this.tokenizer.tokenType().equals(JackTokenizer.tokenType.KEYWORD);
		}
		writeEndTag("statements");
	}
	
	private void compileVarDec() throws IOException, Exception{
			if(this.tokenizer.getCurrentToken().equals("var")) {
				writeStartTag("varDec",true);
				writeKeyword("var");
				this.tokenizer.advance();
				compileParameterList();
				
			writeSymbol(";");
				
		writeEndTag("varDec");
			}
			
	}
	
	
	private void compileIf() throws IOException, Exception{
		writeStartTag("ifStatement",true);
			writeKeyword("if");
			this.tokenizer.advance();
			writeSymbol("(");
				compileExpression(")");
			writeSymbol(")");
			writeSymbol("{");
				compileStatements();
			writeSymbol("}");
			if(this.tokenizer.getCurrentToken().equals("else")) {
				writeKeyword("else");
				this.tokenizer.advance();
				writeSymbol("{");
					compileStatements();
				writeSymbol("}");
			}
		
		writeEndTag("ifStatement");
		
		
	}
	
	private void compileDo() throws IOException, Exception{
		writeStartTag("doStatement",true);
			writeKeyword("do");
			this.tokenizer.advance();
			writeSubroutineCall();
			writeSymbol(";");
		
		writeEndTag("doStatement");
	}
	
	private void compileLet() throws IOException, Exception{
		writeStartTag("letStatement",true);
			writeKeyword("let");
			this.tokenizer.advance();
			writeVarName(false);
			writeSymbol(";");
		writeEndTag("letStatement");
	}

	private void compileWhile() throws IOException, Exception{
		writeStartTag("whileStatement",true);
			writeKeyword("while");
			this.tokenizer.advance();
			writeSymbol("(");
			compileExpression(")");
			writeSymbol(")");
			writeSymbol("{");
			compileStatements();
			writeSymbol("}");
		
		writeEndTag("whileStatement");
		
	}
	
	private void compileReturn() throws IOException, Exception{
		writeStartTag("returnStatement",true);
			writeKeyword("return");
			this.tokenizer.advance();
			if(this.tokenizer.getCurrentToken().equals(";")) {
				writeSymbol(";");
			}else {
				compileExpression(";");
				writeSymbol(";");
			}
			
		
		writeEndTag("returnStatement");
	}
	private void compileExpression(String endChar) throws IOException, Exception{
		writeStartTag("expression", true);
		while(!this.tokenizer.getCurrentToken().equals(endChar)) {
			if(this.tokenizer.tokenType().equals(JackTokenizer.tokenType.SYMBOL)) {
				writeSymbol(this.tokenizer.getCurrentToken());
			}
			compileTerm();
		}
		writeEndTag("expression");
		this.tokenizer.advance();
	
	}
	
	private void compileTerm() throws IOException, Exception{
			
		String currentToken = this.tokenizer.getCurrentToken();
		//expression
		if(currentToken.equals("(")) {
			writeSymbol("(");
			compileExpression(")");
			writeSymbol(")");
			return;
		}
		
		if(currentToken.equals("-") || currentToken.equals("~")){
			writeSymbol(currentToken);
			compileTerm();
			return;
		}
		
		if(currentToken.matches("\\d+")) {
			Integer intConst = Integer.parseInt(currentToken);
			if(intConst < 0 || intConst > 32767 ) {
				throw new Exception("Invalid integer constant");
			}
			writeStartTag("integerConstant", false);
			this.bw.write(currentToken);
			writeEndTag("integerConstant");
			this.tokenizer.advance();
			return;
		}	
		
		if(currentToken.equals("\"")) {
			this.tokenizer.advance();
			String stringConstant = this.tokenizer.getCurrentToken();
			this.tokenizer.advance();
			if((stringConstant.contains("\"") || stringConstant.contains("\n")) && !this.tokenizer.getCurrentToken().equals("\"")) {
				throw new Exception(String.format("%s is an invalid stringconstant", stringConstant));
			}
			writeStartTag("stringConstant", false);
			this.bw.write(stringConstant);
			writeEndTag("stringConstant");
			this.tokenizer.advance();
			return;
		}
		
		if(currentToken.equals("true") || currentToken.equals("false") || currentToken.equals("null") || currentToken.equals("this")) {
			writeStartTag("keywordConstant",false);
			this.bw.write(currentToken);
			writeEndTag("keywordConstant");
			this.tokenizer.advance();
			return;
			
			
		}
		this.tokenizer.advance();
		if(this.tokenizer.getCurrentToken().equals("(")) {
			this.tokenizer.pushBack();
			writeSubroutineCall();
			return;
		}
		
		if(this.tokenizer.getCurrentToken().equals("[")) {
			this.tokenizer.pushBack();
			writeIdentifier();
			writeSymbol("[");
			compileExpression("]");
			writeSymbol("]");
			return;
			

		}
		System.out.println(currentToken);
		System.out.println("pushBack");
		this.tokenizer.pushBack();
		System.out.println(this.tokenizer.getCurrentToken());
		writeIdentifier();
	}
	
	private void writeExpressionList() throws IOException, Exception {
		writeStartTag("expressionList", true);
		compileExpression(",");
		
		boolean hasMore = this.tokenizer.getCurrentToken().equals(",");
		while(hasMore || !this.tokenizer.getCurrentToken().equals(")")) {
			this.tokenizer.advance();
			hasMore  = this.tokenizer.getCurrentToken().equals(",");
		}
		writeEndTag("expressionList");
	}
	private void writeSubroutineCall() throws IOException, Exception{
		writeStartTag("subroutineName",true);
			writeIdentifier();
			if(this.tokenizer.getCurrentToken().equals(".")) {
				writeSymbol(".");
				writeIdentifier();
			}
			writeSymbol("(");
			if(!this.tokenizer.getCurrentToken().equals(")")) {
				writeExpressionList();
			}
			writeSymbol(")");
		writeEndTag("subroutineName");
		
		
	}
	private void writeVarName() throws IOException, Exception{
		writeVarName(true);
	}
	private void writeVarName(boolean checkType) throws IOException, Exception{
		
			if(checkType && !validType(this.tokenizer.getCurrentToken())) {
				throw new Exception("Invalid type");
			}else if(checkType && validType(this.tokenizer.getCurrentToken())) {
			writeType(this.tokenizer.getCurrentToken());
			}
			writeIdentifier();
			if(this.tokenizer.getCurrentToken().equals("[")) {
				writeSymbol("[");
				compileExpression("]");
				writeSymbol("]");
			}
			boolean hasMore = this.tokenizer.getCurrentToken().equals(",");
			while(hasMore) {
				writeSymbol(",");
				writeIdentifier();
				hasMore = this.tokenizer.getCurrentToken().equals(",");
			}
			writeSymbol("=");
			compileExpression("i");
			if(this.tokenizer.getCurrentToken().equals("|")) {
				writeSymbol("|");
				writeIdentifier();
			}
			
	}

	private void writeType(String type) throws IOException ,Exception{
		if(this.tokenizer.tokenType().equals(JackTokenizer.tokenType.KEYWORD)){
			writeKeyword(this.tokenizer.getCurrentToken());
		
		}else {
		writeStartTag("identifier",false);
		this.bw.write(this.tokenizer.getCurrentToken());
		writeEndTag("identifier");
		}
		this.tokenizer.advance();
	}
	private void writeKeyword(String keyWord) throws IOException {
		writeStartTag("keyword",false);
		this.bw.write(keyWord);
		writeEndTag("keyword");
		
	}
	
	private void writeStartTag(String tagName, boolean newLine) throws IOException  {
		this.bw.write(String.format("<%s>", tagName));
		if(newLine) {
			this.bw.write("\n");
		}
	}
	
	private void writeEndTag(String tagName) throws IOException {
		this.bw.write(String.format("</%s>\n", tagName));
		
	}
	
	private void writeIdentifier() throws IOException, Exception {
		if(this.tokenizer.tokenType().equals(JackTokenizer.tokenType.IDENTIFIER)) {

			writeStartTag("identifier",false);
			this.bw.write(this.tokenizer.identifier());
			writeEndTag("identifier");
			this.tokenizer.advance();
		}else {
		throw new Exception(String.format("%s is not an identifier", this.tokenizer.identifier()));
		}
	}
	private void writeSymbol(String symbol) throws IOException ,Exception{
		checkCurrentToken(symbol);
		writeStartTag("symbol",false);
		switch(symbol) {
		case "<":
			symbol = "&lt;";
			break;
		case ">":
			symbol = "&gt;";
			break;
		case "&":
			symbol = "&amp;";
			break;
		case "\"":
			symbol = "&quot;";
			break;
			default:
				symbol=symbol;
			
		}
		this.bw.write(symbol);
		writeEndTag("symbol");
	//	this.tokenizer.advance();
	}
	
	
	private void checkCurrentToken(String symbol) throws IOException, Exception{
		String[] arr = new String[] {symbol};
		checkCurrentToken(arr);
	
	}
	
	private void checkCurrentToken(String[] tokens) throws Exception {
		boolean found = false;
		for(String token: tokens) {
		if(this.tokenizer.getCurrentToken().equals(token)) {
			
			found = true;
			this.tokenizer.advance();
			break;
		}
		}
		if(!found) {
			throw new Exception(String.format("Expected %s got %s" , String.join(",", tokens), this.tokenizer.getCurrentToken()));
		}
		
		
		
		
	}
	private boolean validType(String token) {
		boolean result =false;
		if(token.equals("int") || token.equals("char") || token.equals("boolean") || this.tokenizer.tokenType().equals(JackTokenizer.tokenType.IDENTIFIER)) {
			result = true;
			
		}
		return result;
	}
}

