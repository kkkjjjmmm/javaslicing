package kkkjjjmmm.slicer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class DoParser {
	
	/* keep all the arguments as string
	 * args[0] the absolute path of the original file that you write 
	 * e.g. "/home/jiaming/WALA/WALA-START/src/main/java/kkkjjjmmm/test/Example.java"
	 * args[1] the absolute path of the modified file, keep the same class name of your original file
	 * e.g. "/home/jiaming/WALA/WALA-START/src/main/java/kkkjjjmmm/modified/Example.java"
	 * args[2] the package of your modified file 
	 * e.g. "kkkjjjmmm.modified"
	 * */
	public static void main(String[] args) throws IOException, InterruptedException{		
		FileInputStream in = new FileInputStream(args[0]);
		CompilationUnit cu = JavaParser.parse(in);
		cu.accept(new OBSVisitor(), null);
		cu.accept(new SVFVisitor(), null);
		File dir = new File(args[1].substring(0, args[1].lastIndexOf('/')+1));
		if(!dir.exists()) {
			dir.mkdir();
		}
		File file = new File(args[1]);
		if(!file.exists()){
			file.createNewFile();
		}
		cu.setPackageDeclaration(args[2]);
		byte[] bArray = cu.toString().getBytes();
		Path p = FileSystems.getDefault().getPath(args[1]);
		Files.write(p, bArray);
		
//		ProcessBuilder pb = new ProcessBuilder();
//		pb.command("jar", "-cvf", "Example.jar", "Example.class");
//		Process proc = pb.start();
//		proc.waitFor();
	}

}
