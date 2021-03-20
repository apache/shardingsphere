package org.apache.shardingsphere.infra.executor.exec;

import lombok.Getter;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.ExecutorDriverManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.StorageResourceOption;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

@Getter
public class ExecContext {
    
    private String sql;
    
    private List<Object> parameters;
    
    private SqlNode sqlNode;
    
    private ShardingRule shardingRule;
    
    private DatabaseType databaseType;
    
    private ConfigurationProperties props;
    
    private ExecutorDriverManager<Connection, Statement, StatementOption> executorDriverManager;
    
    private StorageResourceOption option;
    
    private boolean holdTransaction;
    
    public ExecContext(final String sql, final ShardingRule shardingRule, final List<Object> parameters, 
                       final SqlNode sqlNode, final DatabaseType databaseType,
                       final ConfigurationProperties props, 
                       final ExecutorDriverManager<Connection, Statement, StatementOption> executorDriverManager,
                       final StorageResourceOption option, final boolean holdTransaction) {
        this.sql = sql;
        this.shardingRule = shardingRule;
        this.parameters = parameters;
        this.sqlNode = sqlNode;
        this.databaseType = databaseType;
        this.props = props;
        this.executorDriverManager = executorDriverManager;
        this.option = option;
        this.holdTransaction = holdTransaction;
    }
}
