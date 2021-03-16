package org.apache.shardingsphere.infra.optimize.sql;

import org.apache.calcite.sql.SqlDynamicParam;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.parser.SqlParserPos;

public class SqlDynamicValueParam<T> extends SqlDynamicParam {
    
    private final T original;
    
    private T actual;
    
    public SqlDynamicValueParam(T original, final int index, final SqlParserPos pos) {
        super(index, pos);
        this.original = original;
        this.actual = original;
    }
    
    @Override
    public void unparse(final SqlWriter writer, final int leftPrec, final int rightPrec) {
        writer.print(String.valueOf(actual));
        writer.setNeedWhitespace(true);
    }
    
    public void setActual(T value) {
        this.actual = value;
    }
    
    public T getOriginal() {
        return this.original;
    }
}
