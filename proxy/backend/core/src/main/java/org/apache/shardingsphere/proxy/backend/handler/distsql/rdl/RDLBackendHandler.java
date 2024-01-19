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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.statement.rdl.RDLStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.DatabaseNameUtils;

import java.sql.SQLException;

/**
 * RDL backend handler.
 *
 * @param <T> type of RDL statement
 */
@RequiredArgsConstructor
@Getter
public abstract class RDLBackendHandler<T extends RDLStatement> implements DistSQLBackendHandler {
    
    private final T sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public final ResponseHeader execute() throws SQLException {
        return execute(ProxyContext.getInstance().getDatabase(DatabaseNameUtils.getDatabaseName(sqlStatement, connectionSession)), sqlStatement);
    }
    
    protected abstract ResponseHeader execute(ShardingSphereDatabase database, T sqlStatement);
}
