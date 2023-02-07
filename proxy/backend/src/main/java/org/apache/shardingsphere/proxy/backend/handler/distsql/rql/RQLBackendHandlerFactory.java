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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

/**
 * RQL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RQLBackendHandlerFactory {
    
    /**
     * Create new instance of RQL backend handler.
     * 
     * @param sqlStatement RQL statement
     * @param connectionSession connection session
     * @return RDL backend handler
     */
    public static ProxyBackendHandler newInstance(final RQLStatement sqlStatement, final ConnectionSession connectionSession) {
        return new RQLBackendHandler<>(sqlStatement, connectionSession);
    }
}
