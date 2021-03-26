package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.JDBCMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Execute JDBC query.
 */
public final class JDBCQueryExecutor extends AbstractExecutor {
    
    private final ExecutionUnit executionUnit;
    
    private final RouteContext routeContext;
    
    private final QueryResultMetaData metaData;
    
    private Executor executor;
    
    public JDBCQueryExecutor(final ExecutionUnit executionUnit, final RouteContext routeContext, 
                             final ExecContext execContext, final QueryResultMetaData metaData) {
        super(execContext);
        this.executionUnit = executionUnit;
        this.routeContext = routeContext;
        this.metaData = metaData;
    }
    
    @Override
    protected void executeInit() {
        try {
            ExecutionGroupContext<JDBCExecutionUnit> executionGroups = prepareExecutionGroup(Arrays.asList(executionUnit));
            this.executor = executeQuery(executionGroups);
        } catch (SQLException ex) {
            throw new ShardingSphereException("jdbc executor init failed", ex);
        }
    }
    
    @Override
    protected boolean executeMove() {
        if (executor.moveNext()) {
            replaceCurrent(executor.current());
            return true;
        }
        return false;
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return metaData;
    }
    
    private Executor executeQuery(final ExecutionGroupContext<JDBCExecutionUnit> executionGroups) throws SQLException {
        ExecutorEngine executorEngine = new ExecutorEngine(getExecContext().getProps().<Integer>getValue(ConfigurationPropertyKey.EXECUTOR_SIZE));
        JDBCExecutor jdbcExecutor = new JDBCExecutor(executorEngine, getExecContext().isHoldTransaction());
        List<QueryResult> results = jdbcExecutor.execute(executionGroups, new JDBCExecutorCallback<QueryResult>(getExecContext().getDatabaseType(), 
                null, true) {
            @Override
            protected QueryResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                ResultSet resultSet;
                if (statement instanceof PreparedStatement) {
                    PreparedStatement pstmt = (PreparedStatement) statement;
                    setParameters(pstmt, getExecContext().getParameters());
                    resultSet = pstmt.executeQuery();
                } else {
                    resultSet = statement.executeQuery(sql);
                }
                return ConnectionMode.MEMORY_STRICTLY == connectionMode ? new JDBCStreamQueryResult(resultSet) : new JDBCMemoryQueryResult(resultSet);
            }
        
            @Override
            protected Optional<QueryResult> getSaneResult(final SQLStatement sqlStatement) {
                return Optional.empty();
            }
        });
        if (results.isEmpty()) {
            return SimpleExecutor.empty(getExecContext(), metaData);
        }
        return wrapQueryResult(getExecContext(), results);
    }
    
    private Executor wrapQueryResult(final ExecContext execContext, final List<QueryResult> queryResults) {
        List<Executor> executors = queryResults.stream().map(queryResult -> new QueryResultExecutor(queryResult, execContext)).collect(Collectors.toList());
        return new MultiExecutor(executors, execContext);
    }
    
    private void setParameters(final PreparedStatement pstmt, final List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            pstmt.setObject(i + 1, parameters.get(i));
        }
    }
    
    private ExecutionGroupContext<JDBCExecutionUnit> prepareExecutionGroup(final Collection<ExecutionUnit> executionUnits) throws SQLException {
        int maxConnectionsSizePerQuery = getExecContext().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
    
        String stmtType = JDBCDriverType.PREPARED_STATEMENT;
        if (getExecContext().getParameters().isEmpty()) {
            stmtType = JDBCDriverType.STATEMENT;
        }
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(stmtType,
                maxConnectionsSizePerQuery, getExecContext().getExecutorJDBCManager(), getExecContext().getOption(),
                Collections.singletonList(getExecContext().getShardingRule()));
        return prepareEngine.prepare(routeContext, executionUnits);
    }
    
}
