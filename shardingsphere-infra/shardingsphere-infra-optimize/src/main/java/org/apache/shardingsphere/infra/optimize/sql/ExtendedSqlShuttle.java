package org.apache.shardingsphere.infra.optimize.sql;

import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.util.SqlShuttle;

public abstract class ExtendedSqlShuttle extends SqlShuttle {
    
    /**
     * Visit {@link SqlCall}.
     * @param call sqlCall
     * @return result of sqlCall
     */
    @Override
    public SqlNode visit(final SqlCall call) {
        if (call.getKind() == SqlKind.SELECT) {
            return visit((SqlSelect) call);
        } else if (call.getKind() == SqlKind.JOIN) {
            return visit((SqlJoin) call);
        } else if (call.getKind() == SqlKind.ORDER_BY) {
            return visit((SqlOrderBy) call);
        }
        
        return super.visit(call);
    }
    
    abstract SqlSelect visit(SqlSelect sqlSelect);
    
    abstract SqlJoin visit(SqlJoin sqlJoin);
    
    abstract SqlOrderBy visit(SqlOrderBy sqlOrderBy);
}
