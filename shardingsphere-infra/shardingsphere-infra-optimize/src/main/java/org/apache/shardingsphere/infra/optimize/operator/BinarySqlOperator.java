package org.apache.shardingsphere.infra.optimize.operator;

import org.apache.calcite.sql.SqlBinaryOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;

/**
 * binary sql operator.
 */
public enum BinarySqlOperator {

    EQUALS("=", SqlStdOperatorTable.EQUALS),

    GREATER_THAN(">", SqlStdOperatorTable.GREATER_THAN),

    GREATER_EQUALS_THAN(">=", SqlStdOperatorTable.GREATER_THAN_OR_EQUAL),

    LESS_THAN("<", SqlStdOperatorTable.LESS_THAN),

    LESS_EQUALS_THAN("<=", SqlStdOperatorTable.LESS_THAN_OR_EQUAL),

    AND("AND", SqlStdOperatorTable.AND),

    NONE("", null);

    private final String operator;

    private final SqlBinaryOperator sqlBinaryOperator;

    BinarySqlOperator(final String operator, final SqlBinaryOperator sqlBinaryOperator) {
        this.operator = operator;
        this.sqlBinaryOperator = sqlBinaryOperator;
    }
    
    /**
     * Get binary operator. 
     * @return sql binary operator
     */
    public SqlBinaryOperator getSqlBinaryOperator() {
        return sqlBinaryOperator;
    }
    
    /**
     * convert string to BinarySqlOperator.
     * @param sqlOperator string type of sqlOperator
     * @return <code>BinarySqlOperator</code>
     */
    public static BinarySqlOperator value(final String sqlOperator) {
        for (BinarySqlOperator val : values()) {
            if (val.operator.equalsIgnoreCase(sqlOperator)) {
                return val;
            }
        }
        throw new UnsupportedOperationException("unsupported sql operator: " + sqlOperator);
    }
}
