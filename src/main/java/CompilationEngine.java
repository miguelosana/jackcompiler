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

			
		}catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void compileClass() throws IOException,Exception {
		checkCurrentToken("class");
		writeStartTag("class");
		writeKeyword("class");
			writeIdentifier();
			writeSymbol("{");
				while(this.tokenizer.keyWord().equals(JackTokenizer.keyWord.STATIC) || this.tokenizer.keyWord().equals(JackTokenizer.keyWord.FIELD)) {
					compileClassVarDec();
				}
			//write the subroutines
			writeSymbol("}");
		
		writeEndTag("class");
		}
		
	
	private void compileClassVarDec() throws IOException,Exception {
		writeStartTag("classVarDec");
			String keyword = this.tokenizer.getCurrentToken();
			writeKeyword(keyword);
			this.tokenizer.advance();
			if(validType(keyword)) {
				writeType(keyword);
				
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
				writeStartTag("subroutineDec");
				writeKeyword(token);
				String currentToken= this.tokenizer.getCurrentToken();
						
					if(currentToken.equals("void") || validType(currentToken)) {
						writeType(currentToken);
						writeIdentifier();
						writeSymbol("(");
						compileParameterList();
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
			boolean hasMore = this.tokenizer.getCurrentToken().equals(",");
			while(hasMore) {
				writeSymbol(",");
				if(validType(this.tokenizer.getCurrentToken())) {
					writeType(this.tokenizer.getCurrentToken());
				}
			}
		}

	}
	
	private void writeSubRoutineBody() throws IOException, Exception{
		writeStartTag("subroutineBody");
		writeSymbol("{");
			writeStartTag("varDec");
			writeIdentifier();
			writeEndTag("varDec");
			compileStatements();
		
		writeSymbol("}");
		writeEndTag("subroutineBody");
	}
	
	private void compileStatements() throws IOException, Exception{
		writeStartTag("statements");
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
				compileReturn();
				break;
			default:
				throw new Exception("Keyword is not a statement");
			}
			hasMore = this.tokenizer.tokenType().equals(JackTokenizer.tokenType.KEYWORD);
		}
		writeEndTag("statements");
	}
	
	private void compileVarDec() throws IOException, Exception{
		writeStartTag("varDec");
			if(this.tokenizer.getCurrentToken().equals("var")) {
				writeKeyword("var");
				this.tokenizer.advance();
				compileParameterList();
				
				
			}
			writeSymbol(";");
			
		writeEndTag("varDec");
	}
	
	
	
	private void compileDo() throws IOException, Exception{
		writeStartTag("doStatement");
			writeKeyword("do");
			writeSubroutineCall();
			writeSymbol(";");
		
		writeEndTag("doStatement");
	}
	
	private void compileLet() throws IOException, Exception{
		writeStartTag("letStatement");
			writeKeyword("let");
			this.tokenizer.advance();
			writeVarName();
			writeSymbol(";");
		writeEndTag("letStatement");
	}

	private void compileWhile() throws IOException, Exception{
		writeStartTag("whileStatement");
			writeKeyword("while");
			this.tokenizer.advance();
			writeSymbol("(");
			compileExpression();
			writeSymbol(")");
			writeSymbol("{");
			compileStatements();
			writeSymbol("}");
		
		writeEndTag("whileStatement");
		
	}
	
	private void compileReturn() throws IOException, Exception{
		writeStartTag("returnStatement");
			writeKeyword("return");
			this.tokenizer.advance();
			if(this.tokenizer.getCurrentToken().equals(";")) {
				writeSymbol(";");
			}else {
				compileExpression();
				writeSymbol(";");
			}
			
		
		writeEndTag("returnStatement");
	}
	private void compileExpression() throws IOException, Exception{
		
	}
	
	private void writeExpressionList() {
		
	}
	private void writeSubroutineCall() throws IOException, Exception{
		writeStartTag("subroutineName");
			writeIdentifier();
			writeSymbol("(");
				writeExpressionList();
			writeSymbol(")");
		writeEndTag("subroutineName");
		
		
	}
	private void writeVarName() throws IOException, Exception{
		
			if(!validType(this.tokenizer.getCurrentToken())) {
				throw new Exception("Invalid type");
			}
			writeType(this.tokenizer.getCurrentToken());
			writeIdentifier();
			boolean hasMore = this.tokenizer.getCurrentToken().equals(",");
			while(hasMore) {
				writeSymbol(",");
				writeIdentifier();
				hasMore = this.tokenizer.getCurrentToken().equals(",");
			}
	}

	private void writeType(String type) throws IOException ,Exception{
		writeStartTag("type");
		this.bw.write(this.tokenizer.getCurrentToken());
		writeEndTag("type");
		this.tokenizer.advance();
	}
	private void writeKeyword(String keyWord) throws IOException {
		writeStartTag("keyword");
		this.bw.write(keyWord);
		writeEndTag("keyword");
		
	}
	
	private void writeStartTag(String tagName) throws IOException  {
		this.bw.write(String.format("<%s>", tagName));
	}
	
	private void writeEndTag(String tagName) throws IOException {
		this.bw.write(String.format("</%s>", tagName));
		
	}
	
	private void writeIdentifier() throws IOException, Exception {
		if(this.tokenizer.tokenType().equals(JackTokenizer.tokenType.IDENTIFIER)) {
			writeStartTag("identifier");
			this.bw.write(this.tokenizer.identifier());
			writeEndTag("identifier");
			this.tokenizer.advance();
		}
		throw new Exception(String.format("%s is not an identifier", this.tokenizer.identifier()));
	}
	private void writeSymbol(String symbol) throws IOException ,Exception{
		checkCurrentToken(symbol);
		writeStartTag("symbol");
		this.bw.write(symbol);
		writeEndTag("symbol");
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
			throw new Exception(String.format("Expected %s got %s" , tokens.toString(), this.tokenizer.getCurrentToken()));
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

