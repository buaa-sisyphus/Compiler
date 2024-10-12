package symbol;

import token.Token;

import java.util.List;

public class ArraySymbol extends Symbol {
    private List<Object> values;

    public ArraySymbol() {}

    public void set(Token token, int scopeNum, boolean isInt, boolean isArray, boolean isConst){
        this.name=token.getContent();
        this.lineNum=token.getLineNum();
        this.scopeNum=scopeNum;
        if(isConst){
            if (isInt){
                if(isArray){
                    this.symbolType=SymbolType.ConstIntArray;
                }else{
                    this.symbolType=SymbolType.ConstInt;
                }
            }else{
                if(isArray){
                    this.symbolType=SymbolType.ConstCharArray;
                }else{
                    this.symbolType=SymbolType.ConstChar;
                }
            }
        }else {
            if (isInt){
                if(isArray){
                    this.symbolType=SymbolType.IntArray;
                }else{
                    this.symbolType=SymbolType.Int;
                }
            }else{
                if(isArray){
                    this.symbolType=SymbolType.CharArray;
                }else{
                    this.symbolType=SymbolType.Char;
                }
            }
        }
    }
}
