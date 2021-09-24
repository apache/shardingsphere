package org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;

/**
 * MySQL show create trigger statement.
 */
@Getter
@Setter
@ToString
public class MySQLShowCreateTriggerStatement extends AbstractSQLStatement implements DALStatement, MySQLStatement {

    private String trigger;
}
