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

package org.apache.shardingsphere.migration.distsql.handler.update;

import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.impl.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.distsql.handler.ral.update.RALUpdater;
import org.apache.shardingsphere.migration.distsql.statement.StopMigrationCheckStatement;

/**
 * Stop migration check updater.
 */
public final class StopMigrationCheckUpdater implements RALUpdater<StopMigrationCheckStatement> {
    
    private final ConsistencyCheckJobAPI jobAPI = new ConsistencyCheckJobAPI();
    
    @Override
    public void executeUpdate(final String databaseName, final StopMigrationCheckStatement sqlStatement) {
        jobAPI.stopByParentJobId(sqlStatement.getJobId());
    }
    
    @Override
    public String getType() {
        return StopMigrationCheckStatement.class.getName();
    }
}
