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

package org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.global;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.global.GlobalRuleDefinitionStatement;
import org.apache.shardingsphere.mode.manager.ContextManager;

/**
 * Global rule definition execute engine.
 */
@RequiredArgsConstructor
public final class GlobalRuleDefinitionExecuteEngine {
    
    private final GlobalRuleDefinitionStatement sqlStatement;
    
    private final ContextManager contextManager;
    
    @SuppressWarnings("rawtypes")
    private final GlobalRuleDefinitionExecutor executor;
    
    /**
     * Execute update.
     */
    @SuppressWarnings("unchecked")
    public void executeUpdate() {
        executor.checkBeforeUpdate(sqlStatement);
        contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService().alterGlobalRuleConfiguration(executor.buildToBeAlteredRuleConfiguration(sqlStatement));
    }
}
