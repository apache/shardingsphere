/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.routing.router.sharding;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.optimizer.OptimizeEngineFactory;
import io.shardingsphere.core.optimizer.condition.ShardingConditions;
import io.shardingsphere.core.parsing.SQLParsingEngine;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowDatabasesStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowTablesStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dal.DALStatement;
import io.shardingsphere.core.parsing.parser.sql.dcl.DCLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.rewrite.SQLBuilder;
import io.shardingsphere.core.rewrite.SQLRewriteEngine;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.type.RoutingEngine;
import io.shardingsphere.core.routing.type.RoutingResult;
import io.shardingsphere.core.routing.type.TableUnit;
import io.shardingsphere.core.routing.type.broadcast.DatabaseBroadcastRoutingEngine;
import io.shardingsphere.core.routing.type.broadcast.InstanceBroadcastRoutingEngine;
import io.shardingsphere.core.routing.type.broadcast.TableBroadcastRoutingEngine;
import io.shardingsphere.core.routing.type.complex.ComplexRoutingEngine;
import io.shardingsphere.core.routing.type.defaultdb.DefaultDatabaseRoutingEngine;
import io.shardingsphere.core.routing.type.ignore.IgnoreRoutingEngine;
import io.shardingsphere.core.routing.type.standard.StandardRoutingEngine;
import io.shardingsphere.core.routing.type.unicast.UnicastRoutingEngine;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;
import io.shardingsphere.core.util.SQLLogger;
import io.shardingsphere.spi.parsing.ParsingHook;
import io.shardingsphere.spi.parsing.SPIParsingHook;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Sharding router with parse.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class ParsingSQLRouter implements ShardingRouter {
    
    private final ShardingRule shardingRule;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    private final DatabaseType databaseType;
    
    private final boolean showSQL;
    
    private final List<Number> generatedKeys = new LinkedList<>();
    
    private final ShardingDataSourceMetaData shardingDataSourceMetaData;
    
    private final ParsingHook parsingHook = new SPIParsingHook();
    
    @Override
    public SQLStatement parse(final String logicSQL, final boolean useCache) {
        parsingHook.start(logicSQL);
        try {
            SQLStatement result = new SQLParsingEngine(databaseType, logicSQL, shardingRule, shardingTableMetaData).parse(useCache);
            parsingHook.finishSuccess();
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            parsingHook.finishFailure(ex);
            throw ex;
        }
    }
    
    @Override
    public SQLRouteResult route(final String logicSQL, final List<Object> parameters, final SQLStatement sqlStatement) {
        GeneratedKey generatedKey = null;
        if (sqlStatement instanceof InsertStatement) {
            generatedKey = getGenerateKey(shardingRule, (InsertStatement) sqlStatement, parameters);
        }
        SQLRouteResult result = new SQLRouteResult(sqlStatement, generatedKey);
        ShardingConditions shardingConditions = OptimizeEngineFactory.newInstance(shardingRule, sqlStatement, parameters, generatedKey).optimize();
        if (null != generatedKey) {
            setGeneratedKeys(result, generatedKey);
        }
        RoutingResult routingResult = route(sqlStatement, shardingConditions);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, logicSQL, databaseType, sqlStatement, shardingConditions, parameters);
        boolean isSingleRouting = routingResult.isSingleRouting();
        if (sqlStatement instanceof SelectStatement && null != ((SelectStatement) sqlStatement).getLimit()) {
            processLimit(parameters, (SelectStatement) sqlStatement, isSingleRouting);
        }
        SQLBuilder sqlBuilder = rewriteEngine.rewrite(!isSingleRouting);
        for (TableUnit each : routingResult.getTableUnits().getTableUnits()) {
            result.getRouteUnits().add(new RouteUnit(each.getDataSourceName(), rewriteEngine.generateSQL(each, sqlBuilder, shardingDataSourceMetaData)));
        }
        if (showSQL) {
            SQLLogger.logSQL(logicSQL, sqlStatement, result.getRouteUnits());
        }
        return result;
    }
    
    private RoutingResult route(final SQLStatement sqlStatement, final ShardingConditions shardingConditions) {
        Collection<String> tableNames = sqlStatement.getTables().getTableNames();
        RoutingEngine routingEngine;
        if (sqlStatement instanceof UseStatement) {
            routingEngine = new IgnoreRoutingEngine();
        } else if (sqlStatement instanceof DDLStatement || (sqlStatement instanceof DCLStatement && ((DCLStatement) sqlStatement).isGrantForSingleTable())) {
            routingEngine = new TableBroadcastRoutingEngine(shardingRule, sqlStatement);
        } else if (sqlStatement instanceof ShowDatabasesStatement || sqlStatement instanceof ShowTablesStatement) {
            routingEngine = new DatabaseBroadcastRoutingEngine(shardingRule);
        } else if (sqlStatement instanceof DCLStatement) {
            routingEngine = new InstanceBroadcastRoutingEngine(shardingRule, shardingDataSourceMetaData);
        } else if (shardingRule.isAllInDefaultDataSource(tableNames)) {
            routingEngine = new DefaultDatabaseRoutingEngine(shardingRule, tableNames);
        } else if (shardingConditions.isAlwaysFalse()) {
            routingEngine = new UnicastRoutingEngine(shardingRule, tableNames);
        } else if (sqlStatement instanceof DALStatement) {
            routingEngine = new UnicastRoutingEngine(shardingRule, tableNames);
        } else if (tableNames.isEmpty() && sqlStatement instanceof SelectStatement) {
            routingEngine = new UnicastRoutingEngine(shardingRule, tableNames);
        } else if (tableNames.isEmpty()) {
            routingEngine = new DatabaseBroadcastRoutingEngine(shardingRule);
        } else if (1 == tableNames.size() || shardingRule.isAllBindingTables(tableNames)) {
            routingEngine = new StandardRoutingEngine(shardingRule, tableNames.iterator().next(), shardingConditions);
        } else {
            // TODO config for cartesian set
            routingEngine = new ComplexRoutingEngine(shardingRule, tableNames, shardingConditions);
        }
        return routingEngine.route();
    }
    
    private GeneratedKey getGenerateKey(final ShardingRule shardingRule, final InsertStatement insertStatement, final List<Object> parameters) {
        GeneratedKey result = null;
        if (-1 != insertStatement.getGenerateKeyColumnIndex()) {
            for (GeneratedKeyCondition generatedKeyCondition : insertStatement.getGeneratedKeyConditions()) {
                if (null == result) {
                    result = new GeneratedKey(generatedKeyCondition.getColumn());
                }
                if (-1 == generatedKeyCondition.getIndex()) {
                    result.getGeneratedKeys().add(generatedKeyCondition.getValue());
                } else {
                    result.getGeneratedKeys().add((Number) parameters.get(generatedKeyCondition.getIndex()));
                }
            }
            return result;
        }
        String logicTableName = insertStatement.getTables().getSingleTableName();
        Optional<TableRule> tableRule = shardingRule.tryFindTableRuleByLogicTable(logicTableName);
        if (!tableRule.isPresent()) {
            return null;
        }
        Optional<Column> generateKeyColumn = shardingRule.getGenerateKeyColumn(logicTableName);
        if (generateKeyColumn.isPresent()) {
            result = new GeneratedKey(generateKeyColumn.get());
            for (int i = 0; i < insertStatement.getInsertValues().getInsertValues().size(); i++) {
                result.getGeneratedKeys().add(shardingRule.generateKey(logicTableName));
            }
        }
        return result;
    }
    
    private void setGeneratedKeys(final SQLRouteResult sqlRouteResult, final GeneratedKey generatedKey) {
        generatedKeys.addAll(generatedKey.getGeneratedKeys());
        sqlRouteResult.getGeneratedKey().getGeneratedKeys().clear();
        sqlRouteResult.getGeneratedKey().getGeneratedKeys().addAll(generatedKeys);
    }
    
    private void processLimit(final List<Object> parameters, final SelectStatement selectStatement, final boolean isSingleRouting) {
        if (isSingleRouting) {
            selectStatement.setLimit(null);
            return;
        }
        boolean isNeedFetchAll = (!selectStatement.getGroupByItems().isEmpty() || !selectStatement.getAggregationSelectItems().isEmpty()) && !selectStatement.isSameGroupByAndOrderByItems();
        selectStatement.getLimit().processParameters(parameters, isNeedFetchAll);
    }
}
