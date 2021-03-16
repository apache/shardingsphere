package org.apache.shardingsphere.infra.executor.exec.func;

import org.apache.calcite.adapter.enumerable.NullPolicy;
import org.apache.calcite.sql.SqlAggFunction;
import org.apache.calcite.sql.SqlOperator;
import org.apache.shardingsphere.infra.executor.exec.func.implementor.AggFunctionImplementor;
import org.apache.shardingsphere.infra.executor.exec.func.implementor.BinaryFunctionImplementor;
import org.apache.shardingsphere.infra.executor.exec.func.implementor.RexCallToFunctionImplementor;

import java.util.HashMap;
import java.util.Map;

import static org.apache.calcite.sql.fun.SqlStdOperatorTable.COUNT;
import static org.apache.calcite.sql.fun.SqlStdOperatorTable.DIVIDE;
import static org.apache.calcite.sql.fun.SqlStdOperatorTable.DIVIDE_INTEGER;
import static org.apache.calcite.sql.fun.SqlStdOperatorTable.EQUALS;
import static org.apache.calcite.sql.fun.SqlStdOperatorTable.GREATER_THAN;
import static org.apache.calcite.sql.fun.SqlStdOperatorTable.GREATER_THAN_OR_EQUAL;
import static org.apache.calcite.sql.fun.SqlStdOperatorTable.LESS_THAN;
import static org.apache.calcite.sql.fun.SqlStdOperatorTable.LESS_THAN_OR_EQUAL;
import static org.apache.calcite.sql.fun.SqlStdOperatorTable.MINUS;
import static org.apache.calcite.sql.fun.SqlStdOperatorTable.MULTIPLY;
import static org.apache.calcite.sql.fun.SqlStdOperatorTable.NOT_EQUALS;
import static org.apache.calcite.sql.fun.SqlStdOperatorTable.PLUS;

/**
 * Contains implementations of Rex operators as {@link BuiltinFunction}.
 */
public class BuiltinFunctionTable {
    
    public static BuiltinFunctionTable INSTANCE = new BuiltinFunctionTable();
    
    private final Map<SqlOperator, RexCallToFunctionImplementor> map = new HashMap<>();
    
    private final Map<SqlOperator, AggFunctionImplementor> aggMap = new HashMap<>();
    
    private BuiltinFunctionTable() {
        // comparisons
        defineBinary(LESS_THAN, NullPolicy.STRICT, "lt");
        defineBinary(LESS_THAN_OR_EQUAL, NullPolicy.STRICT, "le");
        defineBinary(GREATER_THAN, NullPolicy.STRICT, "gt");
        defineBinary(GREATER_THAN_OR_EQUAL, NullPolicy.STRICT, "ge");
        defineBinary(EQUALS, NullPolicy.STRICT, "eq");
        defineBinary(NOT_EQUALS, NullPolicy.STRICT, "ne");
    
        // arithmetic
        defineBinary(PLUS, NullPolicy.STRICT, "plus");
        defineBinary(MINUS, NullPolicy.STRICT, "minus");
        defineBinary(MULTIPLY, NullPolicy.STRICT, "multiply");
        defineBinary(DIVIDE, NullPolicy.STRICT, "divide");
        defineBinary(DIVIDE_INTEGER, NullPolicy.STRICT, "divide");
    
        aggMap.put(COUNT, new AggFunctionImplementor(NullPolicy.ANY));
    }
    
    public RexCallToFunctionImplementor get(final SqlOperator sqlOperator) {
        return map.get(sqlOperator);
    }
    
    public AggFunctionImplementor get(final SqlAggFunction sqlAggFunction) {
        return aggMap.get(sqlAggFunction);
    }
    
    private void defineBinary(SqlOperator operator, NullPolicy nullPolicy, String backupMethodName) {
        map.put(operator, new BinaryFunctionImplementor(nullPolicy, backupMethodName));
    }
    
    
}
