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

package org.apache.shardingsphere.core.routing.router.sharding;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import org.apache.shardingsphere.core.optimizer.result.condition.ShardingConditions;
import org.apache.shardingsphere.core.parsing.antlr.sql.statement.dcl.DCLStatement;
import org.apache.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowDatabasesStatement;
import org.apache.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowTableStatusStatement;
import org.apache.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowTablesStatement;
import org.apache.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import org.apache.shardingsphere.core.parsing.parser.dialect.postgresql.statement.ResetParamStatement;
import org.apache.shardingsphere.core.parsing.parser.dialect.postgresql.statement.SetParamStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.routing.type.RoutingEngine;
import org.apache.shardingsphere.core.routing.type.broadcast.DatabaseBroadcastRoutingEngine;
import org.apache.shardingsphere.core.routing.type.broadcast.InstanceBroadcastRoutingEngine;
import org.apache.shardingsphere.core.routing.type.broadcast.TableBroadcastRoutingEngine;
import org.apache.shardingsphere.core.routing.type.complex.ComplexRoutingEngine;
import org.apache.shardingsphere.core.routing.type.defaultdb.DefaultDatabaseRoutingEngine;
import org.apache.shardingsphere.core.routing.type.ignore.IgnoreRoutingEngine;
import org.apache.shardingsphere.core.routing.type.standard.StandardRoutingEngine;
import org.apache.shardingsphere.core.routing.type.unicast.UnicastRoutingEngine;
import org.apache.shardingsphere.core.rule.ShardingRule;

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
        if (SQLType.TCL == sqlStatement.getType()) {
            return new DatabaseBroadcastRoutingEngine(shardingRule);
        }
        if (SQLType.DDL == sqlStatement.getType()) {
            return new TableBroadcastRoutingEngine(shardingRule, sqlStatement);
        }
        if (SQLType.DAL == sqlStatement.getType()) {
            return getDALRoutingEngine(shardingRule, sqlStatement, tableNames);
        }
        if (SQLType.DCL == sqlStatement.getType()) {
            return getDCLRoutingEngine(shardingRule, sqlStatement, shardingDataSourceMetaData);
        }
        if (shardingRule.isAllInDefaultDataSource(tableNames)) {
            return new DefaultDatabaseRoutingEngine(shardingRule, tableNames);
        }
        if (shardingRule.isAllBroadcastTables(tableNames)) {
            return SQLType.DQL == sqlStatement.getType() ? new UnicastRoutingEngine(shardingRule, tableNames) : new DatabaseBroadcastRoutingEngine(shardingRule);
        }
        if (shardingConditions.isAlwaysFalse() || tableNames.isEmpty()) {
            return new UnicastRoutingEngine(shardingRule, tableNames);
        }
        Collection<String> shardingTableNames = shardingRule.getShardingLogicTableNames(tableNames);
        if (1 == shardingTableNames.size() || shardingRule.isAllBindingTables(shardingTableNames)) {
            return new StandardRoutingEngine(sqlStatement, shardingRule, shardingTableNames.iterator().next(), shardingConditions);
        }
        // TODO config for cartesian set
        return new ComplexRoutingEngine(sqlStatement, shardingRule, tableNames, shardingConditions);
    }
    
    private static RoutingEngine getDALRoutingEngine(final ShardingRule shardingRule, final SQLStatement sqlStatement, final Collection<String> tableNames) {
        if (sqlStatement instanceof UseStatement) {
            return new IgnoreRoutingEngine();
        }
        if (sqlStatement instanceof ShowDatabasesStatement || ((sqlStatement instanceof ShowTablesStatement || sqlStatement instanceof ShowTableStatusStatement) && tableNames.isEmpty())
                || sqlStatement instanceof SetParamStatement || sqlStatement instanceof ResetParamStatement) {
            return new DatabaseBroadcastRoutingEngine(shardingRule);
        }
        return new UnicastRoutingEngine(shardingRule, tableNames);
    }

    private static RoutingEngine getDCLRoutingEngine(final ShardingRule shardingRule, final SQLStatement sqlStatement, final ShardingDataSourceMetaData shardingDataSourceMetaData) {
        return ((DCLStatement) sqlStatement).isGrantForSingleTable()
                ? new TableBroadcastRoutingEngine(shardingRule, sqlStatement) : new InstanceBroadcastRoutingEngine(shardingRule, shardingDataSourceMetaData);
    }
}
