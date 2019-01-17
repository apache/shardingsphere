/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import io.shardingsphere.core.parsing.parser.sql.dml.DMLStatement;
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
        if (sqlStatement instanceof UseStatement) {
            return new IgnoreRoutingEngine();
        }
        if (isDatabaseAdministrationCommand(sqlStatement, tableNames) || isDMLBroadcastTable(shardingRule, sqlStatement, tableNames)) {
            return new DatabaseBroadcastRoutingEngine(shardingRule);
        }
        if (sqlStatement instanceof DDLStatement || isDCLForTable(sqlStatement)) {
            return new TableBroadcastRoutingEngine(shardingRule, sqlStatement);
        }
        if (sqlStatement instanceof DCLStatement) {
            return new InstanceBroadcastRoutingEngine(shardingRule, shardingDataSourceMetaData);
        }
        if (shardingRule.isAllInDefaultDataSource(tableNames)) {
            return new DefaultDatabaseRoutingEngine(shardingRule, tableNames);
        }
        if (shardingConditions.isAlwaysFalse()) {
            return new UnicastRoutingEngine(shardingRule, tableNames);
        }
        if (sqlStatement instanceof DALStatement) {
            return new UnicastRoutingEngine(shardingRule, tableNames);
        }
        if (isUnicastDQL(shardingRule, sqlStatement, tableNames)) {
            return new UnicastRoutingEngine(shardingRule, tableNames);
        }
        if (tableNames.isEmpty()) {
            return new DatabaseBroadcastRoutingEngine(shardingRule);
        }
        Collection<String> shardingTableNames = shardingRule.getShardingLogicTableNames(tableNames);
        if (1 == shardingTableNames.size() || shardingRule.isAllBindingTables(shardingTableNames)) {
            return new StandardRoutingEngine(shardingRule, shardingTableNames.iterator().next(), shardingConditions);
        }
        // TODO config for cartesian set
        return new ComplexRoutingEngine(shardingRule, tableNames, shardingConditions);
    }
    
    private static boolean isDatabaseAdministrationCommand(final SQLStatement sqlStatement, final Collection<String> tableNames) {
        return sqlStatement instanceof ShowDatabasesStatement || ((sqlStatement instanceof ShowTablesStatement || sqlStatement instanceof ShowTableStatusStatement) && tableNames.isEmpty())
                || sqlStatement instanceof SetParamStatement || sqlStatement instanceof ResetParamStatement;
    }
    
    private static boolean isDMLBroadcastTable(final ShardingRule shardingRule, final SQLStatement sqlStatement, final Collection<String> tableNames) {
        return sqlStatement instanceof DMLStatement && shardingRule.isAllBroadcastTables(tableNames);
    }
    
    private static boolean isDCLForTable(final SQLStatement sqlStatement) {
        return sqlStatement instanceof DCLStatement && ((DCLStatement) sqlStatement).isGrantForSingleTable();
    }
    
    private static boolean isUnicastDQL(final ShardingRule shardingRule, final SQLStatement sqlStatement, final Collection<String> tableNames) {
        return sqlStatement instanceof SelectStatement && (tableNames.isEmpty() || shardingRule.isAllBroadcastTables(tableNames));
    }
}
