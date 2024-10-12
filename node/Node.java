package node;

public abstract class Node {
    protected NodeType type;

    public abstract void print();

    public String typeToString() {
        return "<" + type.toString() + ">" + "\n";
    }
}
