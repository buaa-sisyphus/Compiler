package llvm.values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ValueTable {
    private HashMap<String, Value> valueTable = new HashMap<>();
    private HashMap<String, Integer> constTable = new HashMap<>();
    private ValueTable parentTable;
    private List<ValueTable> childrenTables = new ArrayList<>();

    public ValueTable() {
    }

    public void setParentTable(ValueTable parentTable) {
        this.parentTable = parentTable;
    }

    public void addChild(ValueTable childTable) {
        childrenTables.add(childTable);
    }

    public void addValue(String name, Value value) {
        valueTable.put(name, value);
    }

    public void addConstValue(String name, Integer value) {
        constTable.put(name, value);
    }

    public Value getValue(String name) {
        return valueTable.get(name);
    }

    public Value getValueDeep(String name) {
        Value value = null;
        for (ValueTable table = this; table != null; table = table.parentTable) {
            value = table.getValue(name);
            if (value != null) {
                return value;
            }
        }
        return value;
    }

    public HashMap<String, Value> getValueTable() {
        return valueTable;
    }

    public Integer getConst(String name) {
        return constTable.get(name);
    }

    public Integer getConstDeep(String name) {
        for (ValueTable table = this; table != null; table = table.parentTable) {
            Integer value = table.getConst(name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public ValueTable getParentTable() {
        return parentTable;
    }

}
