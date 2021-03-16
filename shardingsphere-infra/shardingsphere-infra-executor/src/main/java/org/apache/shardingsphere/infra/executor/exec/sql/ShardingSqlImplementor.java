package org.apache.shardingsphere.infra.executor.exec.sql;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.util.Util;

public class ShardingSqlImplementor extends RelToSqlConverter {
    /**
     * Creates a RelToSqlConverter.
     *
     * @param dialect
     */
    public ShardingSqlImplementor(final SqlDialect dialect) {
        super(dialect);
    }
    
    @Override
    public Result visit(final TableScan e) {
        String tableName = Util.last(e.getTable().getQualifiedName());
        SqlIdentifier sqlNode = new SqlIdentifier(ImmutableList.of(tableName), SqlParserPos.ZERO);
        return result(sqlNode, ImmutableList.of(Clause.FROM), e, null);
    }
}
