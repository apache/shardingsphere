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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import com.google.gson.Gson;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.api.InventoryIncrementalJobAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineJobAPI;
import org.apache.shardingsphere.distsql.handler.resultset.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowMigrationRuleStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Result set for show migration rule.
 */
public final class ShowMigrationRuleResultSet implements DatabaseDistSQLResultSet {
    
    private static final Gson GSON = new Gson();
    
    private Iterator<Collection<Object>> data;
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        PipelineProcessConfiguration processConfig = ((InventoryIncrementalJobAPI) TypedSPIRegistry.getRegisteredService(PipelineJobAPI.class, "MIGRATION")).showProcessConfiguration();
        Collection<Object> row = new LinkedList<>();
        row.add(getString(processConfig.getRead()));
        row.add(getString(processConfig.getWrite()));
        row.add(getString(processConfig.getStreamChannel()));
        data = Collections.singletonList(row).iterator();
    }
    
    private String getString(final Object obj) {
        return null == obj ? "" : GSON.toJson(obj);
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("read", "write", "stream_channel");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return ShowMigrationRuleStatement.class.getName();
    }
}
