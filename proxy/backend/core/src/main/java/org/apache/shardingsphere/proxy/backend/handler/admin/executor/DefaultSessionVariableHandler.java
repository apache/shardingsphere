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

package org.apache.shardingsphere.proxy.backend.handler.admin.executor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.util.Collections;

/**
 * Default session variable handler.
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DefaultSessionVariableHandler implements SessionVariableHandler {
    
    private final String databaseType;
    
    @Override
    public final void handle(final ConnectionSession connectionSession, final String variableName, final String assignValue) {
        if (TypedSPILoader.findService(ReplayedSessionVariablesProvider.class, databaseType).map(ReplayedSessionVariablesProvider::getVariables).orElseGet(Collections::emptySet)
                .contains(variableName) || isNeedHandle(variableName)) {
            connectionSession.getRequiredSessionVariableRecorder().setVariable(variableName, assignValue);
        } else {
            log.debug("Set statement {} = {} was discarded.", variableName, assignValue);
        }
    }
    
    protected boolean isNeedHandle(final String variableName) {
        return false;
    }
    
    @Override
    public final boolean isDefault() {
        return true;
    }
}
