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

package org.apache.shardingsphere.data.pipeline.cdc.distsql.handler.update;

import org.apache.shardingsphere.data.pipeline.cdc.api.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.distsql.statement.updatable.DropStreamingStatement;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

/**
 * Drop streaming executor.
 */
public final class DropStreamingExecutor implements DistSQLUpdateExecutor<DropStreamingStatement> {
    
    private final CDCJobAPI jobAPI = (CDCJobAPI) TypedSPILoader.getService(TransmissionJobAPI.class, "STREAMING");
    
    @Override
    public void executeUpdate(final DropStreamingStatement sqlStatement, final ContextManager contextManager) {
        jobAPI.drop(sqlStatement.getJobId());
    }
    
    @Override
    public Class<DropStreamingStatement> getType() {
        return DropStreamingStatement.class;
    }
}
