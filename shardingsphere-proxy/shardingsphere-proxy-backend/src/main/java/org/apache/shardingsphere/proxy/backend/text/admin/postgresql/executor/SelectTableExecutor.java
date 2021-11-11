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

package org.apache.shardingsphere.proxy.backend.text.admin.postgresql.executor;

import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.AbstractDatabaseMetadataExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.AbstractDatabaseMetadataExecutor.DefaultDatabaseMetadataExecutor;

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
    
    private static final String TABLE_NAME = "tablename";
    
    private String actualTableName = "";
    
    private List<String> tableNames;
    
    public SelectTableExecutor(final String sql) {
        super(sql);
    }
    
    @Override
    protected void initSchemaData(final String schemaName) {
        tableNames = new ArrayList<>(ProxyContext.getInstance().getMetaData(schemaName).getSchema().getAllTableNames());
    }
    
    @Override
    protected List<String> getSchemaNames() {
        Collection<String> schemaNames = ProxyContext.getInstance().getAllSchemaNames();
        return schemaNames.stream().filter(AbstractDatabaseMetadataExecutor::hasDatasource).collect(Collectors.toList());
    }
    
    @Override
    protected void rowPostProcessing(final String schemaName, final Map<String, Object> rowMap, final Map<String, String> aliasMap) {
        if (actualTableName.isEmpty()) {
            actualTableName = aliasMap.getOrDefault(REL_NAME, aliasMap.getOrDefault(TABLE_NAME, ""));
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
