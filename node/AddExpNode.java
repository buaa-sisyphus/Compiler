package node;

import symbol.Symbol.SymbolType;
import symbol.SymbolTable;
import token.Token;
import token.TokenType;
import utils.IOUtils;

/* AddExp → MulExp | AddExp ('+' | '−') MulExp
 * ↓
 * AddExp -> MulExp [('+' | '-') AddExp]
 */
public class AddExpNode extends Node {
    private MulExpNode mulExpNode;
    private AddExpNode addExpNode;
    private Token op;

    public AddExpNode(MulExpNode mulExpNode, AddExpNode addExpNode, Token op) {
        this.mulExpNode = mulExpNode;
        this.addExpNode = addExpNode;
        this.op = op;
        this.type = NodeType.AddExp;
    }

    public void print() {
        mulExpNode.print();
        IOUtils.write(typeToString());
        if (addExpNode != null) {
            IOUtils.write(op.toString());
            addExpNode.print();
        }
    }

    public String getType() {
        if(op!=null){
            return "var";
        }else{
            return mulExpNode.getType();
        }
    }

    public void fill(SymbolTable table) {
        mulExpNode.fill(table);
        if (addExpNode != null) {
            addExpNode.fill(table);
        }
    }
}
