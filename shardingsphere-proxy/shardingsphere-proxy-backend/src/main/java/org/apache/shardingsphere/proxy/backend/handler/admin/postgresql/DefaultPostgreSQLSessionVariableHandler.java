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

package org.apache.shardingsphere.proxy.backend.handler.admin.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.ReplayRequiredSessionVariablesLoader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.util.Collection;

/**
 * Default session variable handler for PostgreSQL.
 */
@Slf4j
public final class DefaultPostgreSQLSessionVariableHandler implements PostgreSQLSessionVariableHandler {
    
    private final Collection<String> replayRequiredSessionVariables = ReplayRequiredSessionVariablesLoader.getVariables("PostgreSQL");
    
    @Override
    public void handle(final ConnectionSession connectionSession, final String variableName, final String assignValue) {
        if (replayRequiredSessionVariables.contains(variableName)) {
            connectionSession.getRequiredSessionVariableRecorder().setVariable(variableName, assignValue);
        } else {
            log.debug("Set statement {} = {} was discarded.", variableName, assignValue);
        }
    }
}
