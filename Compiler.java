import error.ErrorHandler;
import frontend.Builder;
import frontend.Lexer;
import frontend.Parser;
import utils.IOUtils;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        IOUtils.empty("parser.txt");
        IOUtils.empty("lexer.txt");
        IOUtils.empty("error.txt");
        IOUtils.empty("symbol.txt");
        ErrorHandler handler = ErrorHandler.getInstance();
        Lexer lexer = Lexer.getLexer();
        lexer.analyze();
        Parser parser = new Parser(lexer.getTokens());
        parser.analyze();
        Builder builder = Builder.getInstance();
        builder.build(parser.getCompUnitNode());
        if(handler.getErrors().isEmpty()) IOUtils.writeSymbol(builder.getRootTable());
        else IOUtils.writeErrors(handler.getErrors());
    }
}
