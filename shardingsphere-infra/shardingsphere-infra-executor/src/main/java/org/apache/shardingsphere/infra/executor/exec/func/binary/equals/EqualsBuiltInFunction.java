package org.apache.shardingsphere.infra.executor.exec.func.binary.equals;

import org.apache.calcite.sql.SqlKind;
import org.apache.shardingsphere.infra.executor.exec.func.binary.BinaryBuiltinFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EqualsBuiltInFunction extends BinaryBuiltinFunction<Object, Boolean> {
    
    public static final EqualsBuiltInFunction INSTANCE = new EqualsBuiltInFunction();
    
    protected EqualsBuiltInFunction() {
        
    }
    
    @Override
    public String getFunctionName() {
        return SqlKind.EQUALS.name();
    }
    
    @Override
    public Boolean apply(final Object t1, final Object t2) {
        return Objects.equals(t1, t2);
    }
    
    @Override
    public List<String[]> getArgTypeNames() {
        List<String[]> argTypeNames = new ArrayList<>();
        argTypeNames.add(new String[]{"int", "int"});
        argTypeNames.add(new String[]{"long", "long"});
        argTypeNames.add(new String[]{"java.lang.Long", "java.lang.Long"});
        argTypeNames.add(new String[]{"java.lang.Integer", "java.lang.Integer"});
        argTypeNames.add(new String[]{"java.lang.String", "java.lang.String"});
        argTypeNames.add(new String[]{"java.lang.Object", "java.lang.Object"});
        return argTypeNames;
    }
}
