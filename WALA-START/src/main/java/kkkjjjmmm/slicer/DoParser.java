package kkkjjjmmm.slicer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class DoParser {
	
	/* keep all the arguments as string
	 * args[0] the absolute path of the original file that you write 
	 * e.g. "/home/jiaming/WALA/WALA-START/src/main/java/kkkjjjmmm/test/Example.java"
	 * args[1] the output folder where the transformed source file will be written
	 * e.g. "/home/jiaming/WALA/WALA-START/output"
	 * args[2] the package of your modified file 
	 * e.g. "kkkjjjmmm.modified"
	 * */
	public static void main(String[] args) throws IOException, InterruptedException{		
		String inputName = args[0];
		FileInputStream in = new FileInputStream(inputName);
		CompilationUnit cu = JavaParser.parse(in);
		cu.accept(new OBSVisitor(), null);
		cu.accept(new SVFVisitor(), null);
		cu.accept(new MethodVisitor(), null);
		
		File output = new File(args[1]);
		if (!output.isDirectory()) {
			throw new RuntimeException(output + " is not a directory");
		}
		
		String packageDec = cu.getPackageDeclaration().get().getNameAsString();
		String className = inputName.substring(inputName.lastIndexOf('/') + 1);
		File outputFile = new File(output,packageDec.replace('.', '/') + "/" + className);
		System.out.println(outputFile);
		
		byte[] bArray = cu.toString().getBytes();
		Path pathToOutput = Paths.get(outputFile.toURI());
		Files.createDirectories(pathToOutput.getParent());
		Files.write(pathToOutput, bArray);
		
	}

}
