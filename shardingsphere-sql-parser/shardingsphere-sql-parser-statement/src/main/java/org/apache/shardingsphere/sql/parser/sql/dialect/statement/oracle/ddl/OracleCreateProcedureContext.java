package org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl;

import lombok.ToString;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.OracleStatement;

/**
 * oracle create procedure statement.
 */
@ToString
public class OracleCreateProcedureContext extends AbstractSQLStatement implements DDLStatement, OracleStatement {
}
