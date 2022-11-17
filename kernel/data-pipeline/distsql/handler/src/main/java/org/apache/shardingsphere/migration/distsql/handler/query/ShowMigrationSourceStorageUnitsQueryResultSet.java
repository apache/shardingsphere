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

package org.apache.shardingsphere.migration.distsql.handler.query;

import org.apache.shardingsphere.data.pipeline.api.MigrationJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPIFactory;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationSourceStorageUnitsStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Show migration source storage units query result set.
 */
public final class ShowMigrationSourceStorageUnitsQueryResultSet implements DatabaseDistSQLResultSet {
    
    private static final MigrationJobPublicAPI JOB_API = PipelineJobPublicAPIFactory.getMigrationJobPublicAPI();
    
    private Iterator<Collection<Object>> data;
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        data = JOB_API.listMigrationSourceResources().iterator();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "host", "port", "db", "connection_timeout_milliseconds", "idle_timeout_milliseconds",
                "max_lifetime_milliseconds", "max_pool_size", "min_pool_size", "read_only", "other_attributes");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public String getType() {
        return ShowMigrationSourceStorageUnitsStatement.class.getName();
    }
}
