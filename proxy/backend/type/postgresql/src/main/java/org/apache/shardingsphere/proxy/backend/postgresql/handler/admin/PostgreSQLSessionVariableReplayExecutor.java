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

package org.apache.shardingsphere.proxy.backend.postgresql.handler.admin;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.ReplayedSessionVariablesProvider;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.SessionVariableReplayExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

/**
 * Session variable replay executor for PostgreSQL.
 */
@Slf4j
public final class PostgreSQLSessionVariableReplayExecutor implements SessionVariableReplayExecutor {
    
    @Override
    public void handle(final ConnectionSession connectionSession, final String variableName, final String assignValue) {
        if (DatabaseTypedSPILoader.findService(ReplayedSessionVariablesProvider.class, getType()).map(optional -> optional.getVariables().contains(variableName)).orElse(false)) {
            connectionSession.getRequiredSessionVariableRecorder().setVariable(variableName, assignValue);
        } else {
            log.debug("Set statement {} = {} was discarded.", variableName, assignValue);
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
