package utils;

import token.Token;
import error.Error;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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

    public static void wirteErrors(List<Error> errors) throws FileNotFoundException, UnsupportedEncodingException {
        File outputFile = new File("error.txt");
        PrintWriter out = new PrintWriter(outputFile, "UTF-8");
        for (Error error : errors) {
            out.print(error);
        }
        out.close();
    }


}
