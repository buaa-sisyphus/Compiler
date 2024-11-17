package llvm.values;

public class UniqueIdGen {
    private static final UniqueIdGen instance = new UniqueIdGen();
    private int count;

    private UniqueIdGen() {
        count = 0;
    }

    public static UniqueIdGen getInstance() {
        return instance;
    }

    public String getUniqueId() {
        return "unique" + ++count;
    }
}
