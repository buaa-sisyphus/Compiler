package llvm.values;

import llvm.types.Type;

public class Argument extends Value {
    private Function belongFunc;
    private int index;//第几个参数

    public Argument(Type type, int index, Function function, boolean isLibraryFunction) {
        super(isLibraryFunction ? "" : "%arg_" + REG_NUMBER++, type);
        this.index = index;
        this.belongFunc = function;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return this.getType().toString() + " " + this.getName();
    }
}
