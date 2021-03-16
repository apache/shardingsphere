package org.apache.shardingsphere.infra.executor.exec.func.binary.equals;

import org.apache.calcite.sql.SqlKind;
import org.apache.shardingsphere.infra.executor.exec.func.binary.BinaryBuiltinFunction;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class BigDecimalEqualsBuiltinFunction extends BinaryBuiltinFunction<BigDecimal, Boolean> {
    
    public static final BigDecimalEqualsBuiltinFunction INSTANCE = new BigDecimalEqualsBuiltinFunction();
    
    @Override
    public Boolean apply(final BigDecimal t1, final BigDecimal t2) {
        return t1 == t2;
    }
    
    @Override
    public String getFunctionName() {
        return SqlKind.EQUALS.name();
    }
    
    public List<String[]> getArgTypeNames() {
        String typeName = BigDecimal.class.getTypeName();
        return Collections.singletonList(new String[]{typeName, typeName});
    }
}