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

package org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.handler.update;

import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.CommitMigrationStatement;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;

/**
 * Commit migration executor.
 */
public final class CommitMigrationExecutor implements DistSQLUpdateExecutor<CommitMigrationStatement> {
    
    @Override
    public void executeUpdate(final CommitMigrationStatement sqlStatement, final ContextManager contextManager) throws SQLException {
        TypedSPILoader.getService(TransmissionJobAPI.class, "MIGRATION").commit(sqlStatement.getJobId());
    }
    
    @Override
    public Class<CommitMigrationStatement> getType() {
        return CommitMigrationStatement.class;
    }
}
