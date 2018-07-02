import java.io.File;
import java.io.IOException;

public class JackAnalyzer {

	public static void main(String[] args) {
		try {

			File file = new File(args[0]);

			
			JackTokenizer tokenizer = new JackTokenizer();
			CompilationEngine compEngine = new CompilationEngine(tokenizer);
			
			if(file.isDirectory()) {
				for(final File inputFile: file.listFiles()) {
					if(inputFile.getName().endsWith(".jack")) {
						processFile(inputFile, tokenizer, compEngine);
					}
				}	
				}else {
					
					if(file.getName().endsWith(".jack")) {
						processFile(file, tokenizer, compEngine);
					}
				
				
			}
		}catch(IOException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void processFile(File inputFile, JackTokenizer tokenizer, CompilationEngine compEngine) throws IOException, Exception {
		
					String outputFileName = inputFile.getAbsolutePath().replace(".jack", ".xml");

					tokenizer.setInputFile(inputFile);
					compEngine.setFileName(outputFileName);
					tokenizer.advance();
					compEngine.compileClass();

	}
}
