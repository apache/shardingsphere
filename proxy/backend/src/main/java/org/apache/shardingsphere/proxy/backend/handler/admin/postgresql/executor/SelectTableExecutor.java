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

package org.apache.shardingsphere.proxy.backend.handler.admin.postgresql.executor;

import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.AbstractDatabaseMetadataExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.AbstractDatabaseMetadataExecutor.DefaultDatabaseMetadataExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Database metadata query executor, used to query table.
 */
public final class SelectTableExecutor extends DefaultDatabaseMetadataExecutor {
    
    private static final String REL_NAME = "relname";
    
    private static final String REF_NAME = "refname";
    
    private static final String TABLE_NAME = "tablename";
    
    private static final String NAME = "name";
    
    private String actualTableName = "";
    
    private List<String> tableNames;
    
    public SelectTableExecutor(final String sql) {
        super(sql);
    }
    
    @Override
    protected void initDatabaseData(final String databaseName) {
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(databaseName);
        String schemaName = DatabaseTypeEngine.getDefaultSchemaName(database.getProtocolType(), databaseName);
        tableNames = new ArrayList<>(database.getSchema(schemaName).getAllTableNames());
    }
    
    @Override
    protected List<String> getDatabaseNames(final ConnectionSession connectionSession) {
        Collection<String> databaseNames = ProxyContext.getInstance().getAllDatabaseNames().stream().filter(each -> hasAuthority(each, connectionSession.getGrantee())).collect(Collectors.toList());
        return databaseNames.stream().filter(AbstractDatabaseMetadataExecutor::hasDataSource).collect(Collectors.toList());
    }
    
    @Override
    protected void rowPostProcessing(final String databaseName, final Map<String, Object> rowMap, final Map<String, String> aliasMap) {
        if (actualTableName.isEmpty()) {
            actualTableName = aliasMap.getOrDefault(REL_NAME, aliasMap.getOrDefault(TABLE_NAME, aliasMap.getOrDefault(NAME, aliasMap.getOrDefault(REF_NAME, ""))));
        }
    }
    
    @Override
    protected void createPreProcessing() {
        if (actualTableName.isEmpty()) {
            return;
        }
        if (tableNames.size() > getRows().size()) {
            return;
        }
        List<Map<String, Object>> subList = new LinkedList<>(getRows().subList(0, tableNames.size()));
        for (int i = 0; i < subList.size(); i++) {
            subList.get(i).replace(actualTableName, tableNames.get(i));
        }
        getRows().clear();
        getRows().addAll(subList);
    }
}
