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

import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.optimizer.condition.ShardingConditions;
import io.shardingsphere.core.parsing.antlr.sql.statement.dcl.DCLStatement;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.DDLStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowDatabasesStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowTableStatusStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowTablesStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.statement.ResetParamStatement;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.statement.SetParamStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dal.DALStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.routing.type.RoutingEngine;
import io.shardingsphere.core.routing.type.broadcast.DatabaseBroadcastRoutingEngine;
import io.shardingsphere.core.routing.type.broadcast.InstanceBroadcastRoutingEngine;
import io.shardingsphere.core.routing.type.broadcast.TableBroadcastRoutingEngine;
import io.shardingsphere.core.routing.type.complex.ComplexRoutingEngine;
import io.shardingsphere.core.routing.type.defaultdb.DefaultDatabaseRoutingEngine;
import io.shardingsphere.core.routing.type.ignore.IgnoreRoutingEngine;
import io.shardingsphere.core.routing.type.standard.StandardRoutingEngine;
import io.shardingsphere.core.routing.type.unicast.UnicastRoutingEngine;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * Routing engine factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoutingEngineFactory {
    
    /**
     * Create new instance of routing engine.
     * 
     * @param shardingRule sharding rule
     * @param shardingDataSourceMetaData sharding data source meta data
     * @param sqlStatement sql statement
     * @param shardingConditions sharding conditions
     * @return new instance of routing engine
     */
    public static RoutingEngine newInstance(final ShardingRule shardingRule, 
                                            final ShardingDataSourceMetaData shardingDataSourceMetaData, final SQLStatement sqlStatement, final ShardingConditions shardingConditions) {
        Collection<String> tableNames = sqlStatement.getTables().getTableNames();
        RoutingEngine result;
        if (sqlStatement instanceof UseStatement) {
            result = new IgnoreRoutingEngine();
        } else if (shardingRule.isAllBroadcastTables(tableNames) && !(sqlStatement instanceof SelectStatement)) {
            result = new DatabaseBroadcastRoutingEngine(shardingRule);
        } else if (sqlStatement instanceof DDLStatement || (sqlStatement instanceof DCLStatement && ((DCLStatement) sqlStatement).isGrantForSingleTable())) {
            result = new TableBroadcastRoutingEngine(shardingRule, sqlStatement);
        } else if (sqlStatement instanceof ShowDatabasesStatement || ((sqlStatement instanceof ShowTablesStatement || sqlStatement instanceof ShowTableStatusStatement) && tableNames.isEmpty())
                || sqlStatement instanceof SetParamStatement || sqlStatement instanceof ResetParamStatement) {
            result = new DatabaseBroadcastRoutingEngine(shardingRule);
        } else if (sqlStatement instanceof DCLStatement) {
            result = new InstanceBroadcastRoutingEngine(shardingRule, shardingDataSourceMetaData);
        } else if (shardingRule.isAllInDefaultDataSource(tableNames)) {
            result = new DefaultDatabaseRoutingEngine(shardingRule, tableNames);
        } else if (shardingConditions.isAlwaysFalse()) {
            result = new UnicastRoutingEngine(shardingRule, tableNames);
        } else if (sqlStatement instanceof DALStatement) {
            result = new UnicastRoutingEngine(shardingRule, tableNames);
        } else if (tableNames.isEmpty() && sqlStatement instanceof SelectStatement || shardingRule.isAllBroadcastTables(tableNames) && sqlStatement instanceof SelectStatement) {
            result = new UnicastRoutingEngine(shardingRule, tableNames);
        } else if (tableNames.isEmpty()) {
            result = new DatabaseBroadcastRoutingEngine(shardingRule);
        } else if (1 == tableNames.size() || shardingRule.isAllBindingTables(tableNames)) {
            result = new StandardRoutingEngine(shardingRule, tableNames.iterator().next(), shardingConditions);
        } else {
            // TODO config for cartesian set
            result = new ComplexRoutingEngine(shardingRule, tableNames, shardingConditions);
        }
        return result;
    }
}
