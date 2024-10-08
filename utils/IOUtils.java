package utils;

import org.w3c.dom.Node;
import token.Token;
import error.Error;

import java.io.*;
import java.util.List;

public class IOUtils {
    public static void writeTokens(List<Token> tokens) throws FileNotFoundException, UnsupportedEncodingException {
        File outputFile = new File("lexer.txt");
        PrintWriter out = new PrintWriter(outputFile, "UTF-8");
        for (Token token : tokens) {
            out.print(token);
        }
        out.close();
    }

    public static void writeErrors(List<Error> errors) throws FileNotFoundException, UnsupportedEncodingException {
        File outputFile = new File("error.txt");
        PrintWriter out = new PrintWriter(outputFile, "UTF-8");
        for (Error error : errors) {
            out.print(error);
        }
        out.close();
    }

    public static void write(String str) {
        File outputFile = new File("parser.txt");
        FileWriter fw= null;
        try{
            fw = new FileWriter(outputFile,true);
            fw.write(str);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void empty(String filePath){
        File outputFile = new File(filePath);
        FileWriter fw = null;
        try {
            fw = new FileWriter(outputFile, false);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
