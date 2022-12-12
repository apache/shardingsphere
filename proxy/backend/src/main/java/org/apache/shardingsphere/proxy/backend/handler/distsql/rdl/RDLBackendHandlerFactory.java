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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.StorageUnitDefinitionStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.AlterStorageUnitBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.RegisterStorageUnitBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.UnregisterStorageUnitBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.RuleDefinitionBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

/**
 * RDL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RDLBackendHandlerFactory {
    
    /**
     * Create new instance of RDL backend handler.
     * 
     * @param sqlStatement RDL statement
     * @param connectionSession connection session
     * @return RDL backend handler
     */
    public static ProxyBackendHandler newInstance(final RDLStatement sqlStatement, final ConnectionSession connectionSession) {
        if (sqlStatement instanceof StorageUnitDefinitionStatement) {
            return getStorageUnitBackendHandler((StorageUnitDefinitionStatement) sqlStatement, connectionSession);
        }
        return new RuleDefinitionBackendHandler<>((RuleDefinitionStatement) sqlStatement, connectionSession);
    }
    
    private static ProxyBackendHandler getStorageUnitBackendHandler(final StorageUnitDefinitionStatement sqlStatement, final ConnectionSession connectionSession) {
        if (sqlStatement instanceof RegisterStorageUnitStatement) {
            return new RegisterStorageUnitBackendHandler((RegisterStorageUnitStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof AlterStorageUnitStatement) {
            return new AlterStorageUnitBackendHandler((AlterStorageUnitStatement) sqlStatement, connectionSession);
        }
        return new UnregisterStorageUnitBackendHandler((UnregisterStorageUnitStatement) sqlStatement, connectionSession);
    }
}
