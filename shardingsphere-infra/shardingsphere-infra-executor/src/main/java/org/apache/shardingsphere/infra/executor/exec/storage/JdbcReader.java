package org.apache.shardingsphere.infra.executor.exec.storage;

import com.google.common.collect.Lists;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rel.rel2sql.SqlImplementor.Result;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.exec.ExecContext;
import org.apache.shardingsphere.infra.executor.exec.Executor;
import org.apache.shardingsphere.infra.executor.exec.MultiExecutor;
import org.apache.shardingsphere.infra.executor.exec.QueryResultExecutor;
import org.apache.shardingsphere.infra.executor.exec.SimpleExecutor;
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
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;
import org.apache.shardingsphere.infra.optimize.sql.ExtractTableNameSqlShuttle;
import org.apache.shardingsphere.infra.optimize.sql.SqlDynamicValueParam;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
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

public class JdbcReader {
    
    public static Executor read(LogicalScan logicalScan, ExecContext execContext) throws SQLException {
        ShardingRule shardingRule = execContext.getShardingRule();
        RouteContext routeContext = logicalScan.route();
    
        RelNode relNode = logicalScan.build();
        SqlDialect sqlDialect = SqlDialects.toSqlDialect(execContext.getDatabaseType());
        RelToSqlConverter relToSqlConverter = new ShardingSqlImplementor(sqlDialect);
        Result result = relToSqlConverter.visitRoot(relNode);
        SqlNode sqlNode = result.asStatement();
        
        ExtractTableNameSqlShuttle extractTableNameSqlShuttle = new ExtractTableNameSqlShuttle();
        SqlNode sqlTemplate = sqlNode.accept(extractTableNameSqlShuttle);
        List<SqlDynamicValueParam<String>> tables = extractTableNameSqlShuttle.getTableNames();
    
        List<Object> parameters = execContext.getParameters();
        Collection<ExecutionUnit> executionUnits = routeContext.getRouteUnits().stream().map(routeUnit -> {
            
            // if all binding table 
            RouteMapper oneTableRout = routeUnit.getTableMappers().iterator().next();
            Map<String, String> routeTableMap = routeUnit.getTableMappers().stream().collect(Collectors.toMap(RouteMapper::getLogicName, RouteMapper::getActualName));
            tables.forEach(table -> {
                if(routeTableMap.containsKey(table.getOriginal())) {
                    table.setActual(routeTableMap.get(table.getOriginal()));
                } else {
                    RouteMapper dbRoute = routeUnit.getDataSourceMapper();
                    Map<String, String> bindingTableMap = shardingRule.getLogicAndActualTablesFromBindingTable(dbRoute.getActualName(), 
                            oneTableRout.getLogicName(), oneTableRout.getActualName(), Arrays.asList(oneTableRout.getLogicName(), table.getOriginal()));
                    table.setActual(bindingTableMap.get(table.getOriginal()));
                }
            });
            String sql = sqlTemplate.toSqlString(sqlDialect).getSql();
            SQLUnit sqlUnit = new SQLUnit(sql, parameters, Lists.newArrayList(routeUnit.getTableMappers()));
            return new ExecutionUnit(routeUnit.getDataSourceMapper().getActualName(), sqlUnit);
        }).collect(Collectors.toList());
    
        int maxConnectionsSizePerQuery = execContext.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        
        String stmtType = JDBCDriverType.PREPARED_STATEMENT;
        if(parameters.isEmpty()) {
            stmtType = JDBCDriverType.STATEMENT;
        }
    
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(stmtType,
                maxConnectionsSizePerQuery, execContext.getExecutorDriverManager(), execContext.getOption(), Collections.singletonList(shardingRule));
        ExecutionGroupContext<JDBCExecutionUnit> executionGroups =  prepareEngine.prepare(routeContext, executionUnits);
    
        ExecutorEngine executorEngine = new ExecutorEngine(execContext.getProps().<Integer>getValue(ConfigurationPropertyKey.EXECUTOR_SIZE));
        JDBCExecutor jdbcExecutor = new JDBCExecutor(executorEngine, execContext.isHoldTransaction());
        List<QueryResult> results = jdbcExecutor.execute(executionGroups, new JDBCExecutorCallback<QueryResult>(execContext.getDatabaseType(), null, true) {
            @Override
            protected QueryResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                ResultSet resultSet;
                if (statement instanceof PreparedStatement) {
                    PreparedStatement pstmt = (PreparedStatement) statement;
                    for(int i = 0; i < parameters.size(); i++) {
                        pstmt.setObject(i+1, parameters.get(i));
                    }
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
        if(results.isEmpty()) {
            return SimpleExecutor.empty(execContext, metaData);
        }
        return wrapQueryResult(execContext, results);
    }
    
    private static Executor wrapQueryResult(ExecContext execContext, List<QueryResult> queryResults) {
        List<Executor> executors = queryResults.stream().map(queryResult -> new QueryResultExecutor(execContext, queryResult)).collect(Collectors.toList());
        return new MultiExecutor(execContext, executors);
    }
}
