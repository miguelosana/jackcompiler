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
			writeIdentifier();
			if(this.tokenizer.getCurrentToken().equals("[")) {
				writeSymbol("[");
				compileExpression("]");
				writeSymbol("]");
			}
			writeSymbol("=");
			
			compileExpression(";");
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
		String curToken = this.tokenizer.getCurrentToken();
			writeStartTag("term",true);
			
			compileTerm();
			writeEndTag("term");
			do {
				if(this.tokenizer.tokenType().equals(JackTokenizer.tokenType.SYMBOL) && this.tokenizer.isOp()) {
					writeSymbol(this.tokenizer.getCurrentToken());
					writeStartTag("term", true);
					compileTerm();
					writeEndTag("term");
				}else {
					break;
				}
			}
			while(true);
			curToken = this.tokenizer.getCurrentToken();
		writeEndTag("expression");
		this.bw.flush();
	
	}
	
	private void compileTerm() throws IOException, Exception{
		this.bw.flush();	
		if(this.tokenizer.tokenType().equals(JackTokenizer.tokenType.IDENTIFIER)){
			String tempToken = this.tokenizer.getCurrentToken();
			this.tokenizer.advance();
			if(this.tokenizer.getCurrentToken().equals("[")) {
				this.tokenizer.pushBack();this.tokenizer.pushBack();
				writeIdentifier();
				this.tokenizer.advance();
				writeSymbol("[");
				compileExpression("]");
				writeSymbol("]");
				return;
				
			}
			if(this.tokenizer.getCurrentToken().equals(".")) {
				this.tokenizer.pushBack();this.tokenizer.pushBack();
				writeIdentifier();
				this.tokenizer.advance();
				writeSymbol(".");
				writeSubroutineCall();
				return;
			}
			if(this.tokenizer.getCurrentToken().equals("(")) {
				this.tokenizer.pushBack();
				writeIdentifier();
				writeSubroutineCall();
				return;
				

			}
			this.tokenizer.pushBack();this.tokenizer.pushBack();
			
			this.bw.flush();
			writeIdentifier();
			this.tokenizer.advance();
			return;

		
		}
		if(this.tokenizer.tokenType().equals(JackTokenizer.tokenType.INT_CONST)) {
			writeStartTag("integerConstant", false);
			this.bw.write(this.tokenizer.getCurrentToken());
			writeEndTag("integerConstant");
			this.tokenizer.advance();
			return;
		}
		if(this.tokenizer.tokenType().equals(JackTokenizer.tokenType.STRING_CONST)) {
			writeStartTag("stringConstant", false);
			this.bw.write(this.tokenizer.getCurrentToken().replaceAll("string-constant:", ""));
			writeEndTag("stringConstant");
			this.tokenizer.advance();
			return;
		}
		if(this.tokenizer.tokenType().equals(JackTokenizer.tokenType.KEYWORD)) {
			writeKeyword(this.tokenizer.getCurrentToken());
			this.tokenizer.advance();
			return;
		}
		if(this.tokenizer.getCurrentToken().equals("-") || this.tokenizer.getCurrentToken().equals("~")) {
			writeStartTag("symbol",false);
			this.bw.write(this.tokenizer.getCurrentToken());
			writeEndTag("symbol");
			this.tokenizer.advance();
			writeStartTag("term", true);
			compileTerm();
			writeEndTag("term");
			return;
			
		}
		if(this.tokenizer.getCurrentToken().equals("(")) {
			writeSymbol("(");
			compileExpression(")");
			writeSymbol(")");
			return;
		}

	}
	
	private void writeExpressionList() throws IOException, Exception {
		compileExpression(",");
		
		boolean hasMore = this.tokenizer.getCurrentToken().equals(",");
		while(hasMore || !this.tokenizer.getCurrentToken().equals(")")) {
			writeSymbol(",");
			compileExpression(",");
			hasMore  = this.tokenizer.getCurrentToken().equals(",");
		}
	}
	private void writeSubroutineCall() throws IOException, Exception{
	//	writeStartTag("subroutineName",true);
			writeIdentifier();
			if(this.tokenizer.getCurrentToken().equals(".")) {
				writeSymbol(".");
				writeIdentifier();
			}
			writeSymbol("(");
			writeStartTag("expressionList", true);
			if(!this.tokenizer.getCurrentToken().equals(")")) {

				writeExpressionList();
			}
			writeEndTag("expressionList");
			writeSymbol(")");
	//	writeEndTag("subroutineName");
		
		
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
			compileExpression(";");
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
	private boolean validType(String token) throws IOException {
		boolean result =false;
		if(token.equals("int") || token.equals("char") || token.equals("boolean") || this.tokenizer.tokenType().equals(JackTokenizer.tokenType.IDENTIFIER)) {
			result = true;
			
		}
		return result;
	}
}

