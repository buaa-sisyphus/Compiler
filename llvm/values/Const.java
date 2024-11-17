package llvm.values;

import llvm.types.Type;

public class Const extends Value{
    public Const(String name, Type type) {
        super(name, type);
    }
}
