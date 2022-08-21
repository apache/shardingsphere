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
import org.apache.shardingsphere.data.pipeline.api.MigrationJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPIFactory;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowMigrationProcessConfigurationStatement;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineProcessConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Show migration process configuration query result set.
 */
public final class ShowMigrationProcessConfigurationQueryResultSet implements DatabaseDistSQLResultSet {
    
    private static final MigrationJobPublicAPI JOB_API = PipelineJobPublicAPIFactory.getMigrationJobPublicAPI();
    
    private static final Gson GSON = new Gson();
    
    private Iterator<Collection<Object>> data;
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        PipelineProcessConfiguration processConfig = JOB_API.showProcessConfiguration();
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
        return ShowMigrationProcessConfigurationStatement.class.getName();
    }
}
