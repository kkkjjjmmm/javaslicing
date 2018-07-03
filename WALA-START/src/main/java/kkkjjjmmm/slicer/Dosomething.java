package kkkjjjmmm.slicer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class Dosomething {
	
	public static void main(String[] args) throws FileNotFoundException {
		FileInputStream in = new FileInputStream("/home/jiaming/WALA/WALA-START/src/main/java/kkkjjjmmm/slicer/Example.java");
		CompilationUnit cu = JavaParser.parse(in);
	    cu.accept(new MyVisitor("SliceL"), null);
	      
	}

}
