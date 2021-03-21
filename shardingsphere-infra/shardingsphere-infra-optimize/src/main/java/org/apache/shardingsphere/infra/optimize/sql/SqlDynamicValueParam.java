package org.apache.shardingsphere.infra.optimize.sql;

import org.apache.calcite.sql.SqlDynamicParam;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.parser.SqlParserPos;

public final class SqlDynamicValueParam<T> extends SqlDynamicParam {
    
    private final T original;
    
    private T actual;
    
    public SqlDynamicValueParam(final T original, final int index, final SqlParserPos pos) {
        super(index, pos);
        this.original = original;
        this.actual = original;
    }
    
    @Override
    public void unparse(final SqlWriter writer, final int leftPrec, final int rightPrec) {
        writer.print(String.valueOf(actual));
        writer.setNeedWhitespace(true);
    }
    
    /**
     * Set the actual value.
     * @param value actual value to use
     */
    public void setActual(final T value) {
        this.actual = value;
    }
    
    /**
     * Get the original value.
     * @return original value
     */
    public T getOriginal() {
        return this.original;
    }
}
