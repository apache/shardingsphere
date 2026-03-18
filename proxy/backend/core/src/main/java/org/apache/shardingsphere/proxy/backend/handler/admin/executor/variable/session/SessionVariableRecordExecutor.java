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

package org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Session variable record executor.
 */
@RequiredArgsConstructor
@Slf4j
public final class SessionVariableRecordExecutor {
    
    private final DatabaseType databaseType;
    
    private final ConnectionSession connectionSession;
    
    /**
     * Record replayed variable.
     *
     * @param variableName variable name
     * @param assignValue assign value
     */
    public void recordVariable(final String variableName, final String assignValue) {
        if (DatabaseTypedSPILoader.findService(ReplayedSessionVariableProvider.class, databaseType).map(optional -> optional.isNeedToReplay(variableName)).orElse(false)) {
            connectionSession.getRequiredSessionVariableRecorder().setVariable(variableName, assignValue);
        } else {
            log.debug("Set statement {} = {} was discarded.", variableName, assignValue);
        }
    }
    
    /**
     * Record replayed variable.
     *
     * @param variables variables
     */
    public void recordVariable(final Map<String, String> variables) {
        Optional<ReplayedSessionVariableProvider> replayedSessionVariableProvider = DatabaseTypedSPILoader.findService(ReplayedSessionVariableProvider.class, databaseType);
        if (!replayedSessionVariableProvider.isPresent()) {
            log.debug("Set statement {} was discarded.", variables);
            return;
        }
        for (Entry<String, String> entry : variables.entrySet()) {
            if (replayedSessionVariableProvider.get().isNeedToReplay(entry.getKey())) {
                connectionSession.getRequiredSessionVariableRecorder().setVariable(entry.getKey(), entry.getValue());
            } else {
                log.debug("Set statement {} = {} was discarded.", entry.getKey(), entry.getValue());
            }
        }
    }
}
