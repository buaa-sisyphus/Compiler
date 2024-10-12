package error;

import frontend.Lexer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ErrorHandler {
    List<Error> errors=new ArrayList<>();
    private static final ErrorHandler instance=new ErrorHandler();
    private ErrorHandler(){}
    public static ErrorHandler getInstance(){
        return instance;
    }

    public void addError(ErrorType errorType,int lineNum){
        errors.add(new Error(errorType,lineNum));
    }

    public List<Error> getErrors(){
        errors.sort(Comparator.comparingInt(Error::getLineNum));
        return errors;
    }
}
