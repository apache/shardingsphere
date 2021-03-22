package org.apache.shardingsphere.infra.executor.exec;

import com.google.common.collect.Lists;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rel.rel2sql.SqlImplementor.Result;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.exec.sql.ShardingSqlImplementor;
import org.apache.shardingsphere.infra.executor.exec.tool.MetaDataConverter;
import org.apache.shardingsphere.infra.executor.exec.tool.SqlDialects;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
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
import org.apache.shardingsphere.infra.optimize.sql.ExtractTableNameSqlShuttle;
import org.apache.shardingsphere.infra.optimize.sql.SqlDynamicValueParam;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Execute JDBC query.
 */
public final class JDBCQueryExecutor extends AbstractExecutor {
    
    private final RelNode relNode;
    
    private final RouteContext routeContext;
    
    private Executor executor;
    
    public JDBCQueryExecutor(final RelNode relNode, final RouteContext routeContext, final ExecContext execContext) {
        super(execContext);
        this.relNode = relNode;
        this.routeContext = routeContext;
    }
    
    @Override
    protected void executeInit() {
        SqlDialect sqlDialect = SqlDialects.toSqlDialect(getExecContext().getDatabaseType());
        SqlNode sqlNode = convertRelNodeToSqlNode(sqlDialect);
        Collection<ExecutionUnit> executionUnits = generateExecutionUnit(sqlNode, sqlDialect);
        try {
            ExecutionGroupContext<JDBCExecutionUnit> executionGroups = prepareExecutionGroup(executionUnits);
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
        return MetaDataConverter.buildMetaData(relNode);
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
        // wraper results to another queryResult
        QueryResultMetaData metaData = MetaDataConverter.buildMetaData(relNode);
        if (results.isEmpty()) {
            return SimpleExecutor.empty(getExecContext(), metaData);
        }
        return wrapQueryResult(getExecContext(), results);
    }
    
    private static Executor wrapQueryResult(final ExecContext execContext, final List<QueryResult> queryResults) {
        List<Executor> executors = queryResults.stream().map(queryResult -> new QueryResultExecutor(queryResult, execContext)).collect(Collectors.toList());
        return new MultiExecutor(executors, execContext);
    }
    
    private static void setParameters(final PreparedStatement pstmt, final List<Object> parameters) throws SQLException {
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
                maxConnectionsSizePerQuery, getExecContext().getExecutorDriverManager(), getExecContext().getOption(),
                Collections.singletonList(getExecContext().getShardingRule()));
        return prepareEngine.prepare(routeContext, executionUnits);
    }
    
    private SqlNode convertRelNodeToSqlNode(final SqlDialect sqlDialect) {
        RelToSqlConverter relToSqlConverter = new ShardingSqlImplementor(sqlDialect);
        Result result = relToSqlConverter.visitRoot(relNode);
        return result.asStatement();
    }
    
    private Collection<ExecutionUnit> generateExecutionUnit(final SqlNode sqlNode, final SqlDialect sqlDialect) {
        ExtractTableNameSqlShuttle extractTableNameSqlShuttle = new ExtractTableNameSqlShuttle();
        SqlNode sqlTemplate = sqlNode.accept(extractTableNameSqlShuttle);
        List<SqlDynamicValueParam<String>> tables = extractTableNameSqlShuttle.getTableNames();
    
        List<Object> parameters = getExecContext().getParameters();
        return routeContext.getRouteUnits().stream().map(routeUnit -> {
            // if all binding table 
            RouteMapper oneTableRout = routeUnit.getTableMappers().iterator().next();
            Map<String, String> routeTableMap = routeUnit.getTableMappers().stream().collect(Collectors.toMap(RouteMapper::getLogicName, RouteMapper::getActualName));
            tables.forEach(table -> {
                if (routeTableMap.containsKey(table.getOriginal())) {
                    table.setActual(routeTableMap.get(table.getOriginal()));
                } else {
                    RouteMapper dbRoute = routeUnit.getDataSourceMapper();
                    Map<String, String> bindingTableMap = getExecContext().getShardingRule().getLogicAndActualTablesFromBindingTable(dbRoute.getActualName(),
                            oneTableRout.getLogicName(), oneTableRout.getActualName(), Arrays.asList(oneTableRout.getLogicName(), table.getOriginal()));
                    table.setActual(bindingTableMap.get(table.getOriginal()));
                }
            });
            String sql = sqlTemplate.toSqlString(sqlDialect).getSql();
            SQLUnit sqlUnit = new SQLUnit(sql, parameters, Lists.newArrayList(routeUnit.getTableMappers()));
            return new ExecutionUnit(routeUnit.getDataSourceMapper().getActualName(), sqlUnit);
        }).collect(Collectors.toList());
    }
}
