package node;

import token.Token;
import utils.IOUtils;

import java.util.List;

// Block → '{' { BlockItem } '}'
public class BlockNode extends Node {
    private Token lBraceToken;
    private List<BlockItemNode> blockItemNodes;
    private Token rBraceToken;

    public BlockNode(Token lBraceToken, List<BlockItemNode> blockItemNodes, Token rBraceToken) {
        this.lBraceToken = lBraceToken;
        this.blockItemNodes = blockItemNodes;
        this.rBraceToken = rBraceToken;
        this.type = NodeType.Block;
    }

    @Override
    public void print() {
        IOUtils.writeSymbol(lBraceToken.toString());
        for (BlockItemNode blockItemNode : blockItemNodes) {
            blockItemNode.print();
        }
        IOUtils.writeSymbol(rBraceToken.toString());
        IOUtils.writeSymbol(typeToString());
    }

    public List<BlockItemNode> getBlockItemNodes() {
        return blockItemNodes;
    }

    public Token getrBraceToken() {
        return rBraceToken;
    }
}
