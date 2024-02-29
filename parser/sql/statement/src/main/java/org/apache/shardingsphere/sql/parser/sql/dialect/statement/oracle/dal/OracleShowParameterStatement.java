package org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dal;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.OracleStatement;

/**
 * Oracle show parameter statement.
 */
@Getter
@Setter
public final class OracleShowParameterStatement extends AbstractSQLStatement implements DALStatement, OracleStatement {
    private String parameterName;
}
