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

package org.apache.shardingsphere.distsql.handler.executor.rql.resource;

import com.google.common.base.CaseFormat;
import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.ShowRulesUsedStorageUnitStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Show rules used storage unit executor.
 */
@Setter
public final class ShowRulesUsedStorageUnitExecutor implements DistSQLQueryExecutor<ShowRulesUsedStorageUnitStatement>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    @Override
    public Collection<String> getColumnNames(final ShowRulesUsedStorageUnitStatement sqlStatement) {
        return Arrays.asList("type", "name");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowRulesUsedStorageUnitStatement sqlStatement, final ContextManager contextManager) {
        return database.getResourceMetaData().getStorageUnits().containsKey(sqlStatement.getStorageUnitName()) ? getRows(sqlStatement) : Collections.emptyList();
    }
    
    @SuppressWarnings("unchecked")
    private Collection<LocalDataQueryResultRow> getRows(final ShowRulesUsedStorageUnitStatement sqlStatement) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        for (InUsedStorageUnitRetriever<ShardingSphereRule> each : ShardingSphereServiceLoader.getServiceInstances(InUsedStorageUnitRetriever.class)) {
            database.getRuleMetaData().findSingleRule(each.getType()).ifPresent(optional -> result.addAll(getRows(sqlStatement, optional, each)));
        }
        return result;
    }
    
    private Collection<LocalDataQueryResultRow> getRows(final ShowRulesUsedStorageUnitStatement sqlStatement,
                                                        final ShardingSphereRule rule, final InUsedStorageUnitRetriever<ShardingSphereRule> retriever) {
        String type = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, retriever.getType().getSimpleName().substring(0, retriever.getType().getSimpleName().indexOf("Rule")));
        return retriever.getInUsedResources(sqlStatement, rule).stream().map(each -> new LocalDataQueryResultRow(type, each)).collect(Collectors.toList());
    }
    
    @Override
    public Class<ShowRulesUsedStorageUnitStatement> getType() {
        return ShowRulesUsedStorageUnitStatement.class;
    }
}
