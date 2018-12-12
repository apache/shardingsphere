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

package io.shardingsphere.core.routing.type;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;

/**
 * Abstract routing engine.
 * 
 * @author duhongjun
 */
public abstract class AbstractRoutingEngine implements RoutingEngine {
    
    protected boolean checkSharding(Optional<SQLStatement> sqlStatement) {
        if(!sqlStatement.isPresent()) {
            return false;
        }
        if(!(sqlStatement.get() instanceof SelectStatement)) {
            return false;
        }
        SelectStatement selectStatement = (SelectStatement) sqlStatement.get();
        return !selectStatement.getSubQueryStatements().isEmpty();
    }
    
    protected void fillTableDatasourceMapping(final Map<String, Set<String>> shardingSelectedDatasource, final List<TableUnit> tableUnits) {
        for(TableUnit each : tableUnits) {
            for(RoutingTable routingTable : each.getRoutingTables()) {
                Set<String> datasources = shardingSelectedDatasource.get(routingTable.getLogicTableName());
                if(null == datasources) {
                    datasources = new HashSet<>();
                    shardingSelectedDatasource.put(routingTable.getLogicTableName(), datasources);
                }
                datasources.add(each.getDataSourceName());
            }
        }
    }
    
    protected void checkTableDatasourceMapping(final Map<String, Set<String>> shardingSelectedDatasource) {
        if(1 >= shardingSelectedDatasource.size()) {
            return;
        }
        Iterator<Entry<String, Set<String>>> iterator = shardingSelectedDatasource.entrySet().iterator();
        Entry<String, Set<String>> fisrtEntry = iterator.next();
        while(iterator.hasNext()) {
            Entry<String, Set<String>> each = iterator.next();
            Preconditions.checkState(each.getValue().size() == fisrtEntry.getValue().size(), "table must in same sharding.");
            for(String datasource : fisrtEntry.getValue()) {
                Preconditions.checkState(each.getValue().contains(datasource), "table must in same sharding.");
            }
        }
    }   
}
