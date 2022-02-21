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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.resource.AddResourceBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.resource.AlterResourceBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.resource.DropResourceBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.rule.RuleDefinitionBackendHandler;

import java.sql.SQLException;

/**
 * RDL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RDLBackendHandlerFactory {
    
    /**
     * Create new instance of RDL backend handler.
     * 
     * @param databaseType database type
     * @param sqlStatement RDL statement
     * @param connectionSession connection session
     * @return RDL backend handler
     * @throws SQLException SQL exception
     */
    public static TextProtocolBackendHandler newInstance(final DatabaseType databaseType, final RDLStatement sqlStatement, final ConnectionSession connectionSession) throws SQLException {
        return createBackendHandler(databaseType, sqlStatement, connectionSession);
    }
    
    private static TextProtocolBackendHandler createBackendHandler(final DatabaseType databaseType, final RDLStatement sqlStatement, final ConnectionSession connectionSession) {
        if (sqlStatement instanceof AddResourceStatement) {
            return new AddResourceBackendHandler(databaseType, (AddResourceStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof AlterResourceStatement) {
            return new AlterResourceBackendHandler(databaseType, (AlterResourceStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof DropResourceStatement) {
            return new DropResourceBackendHandler((DropResourceStatement) sqlStatement, connectionSession);
        }
        return new RuleDefinitionBackendHandler<>((RuleDefinitionStatement) sqlStatement, connectionSession);
    }
}
