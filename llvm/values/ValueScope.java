package llvm.values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ValueScope {
    private HashMap<String, Value> valueTable = new HashMap<>();
    private HashMap<String, Integer> constTable = new HashMap<>();
    private ValueScope parent;
    private List<ValueScope> children = new ArrayList<>();

    public ValueScope() {
    }

    public void setParent(ValueScope parent) {
        this.parent = parent;
    }

    public void addChild(ValueScope childTable) {
        children.add(childTable);
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
        for (ValueScope table = this; table != null; table = table.parent) {
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
        for (ValueScope table = this; table != null; table = table.parent) {
            Integer value = table.getConst(name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public ValueScope getParent() {
        return parent;
    }

}
