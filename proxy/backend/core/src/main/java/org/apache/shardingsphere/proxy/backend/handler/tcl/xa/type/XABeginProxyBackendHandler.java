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

package org.apache.shardingsphere.proxy.backend.handler.tcl.xa.type;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.xa.jta.exception.XATransactionNestedBeginException;

import java.sql.SQLException;

/**
 * XA begin proxy backend handler.
 */
// TODO Currently XA transaction started with `XA START` doesn't support for database with multiple datasource, a flag should be added for this both in init progress and add datasource from DistSQL.
@RequiredArgsConstructor
public final class XABeginProxyBackendHandler implements ProxyBackendHandler {
    
    private final ShardingSphereMetaData metaData;
    
    private final ConnectionSession connectionSession;
    
    private final DatabaseProxyConnector databaseProxyConnector;
    
    /*
     * We have to let session occupy the thread when doing XA transaction. According to https://dev.mysql.com/doc/refman/5.7/en/xa-states.html XA and local transactions are mutually exclusive.
     */
    @Override
    public ResponseHeader execute() throws SQLException {
        ShardingSpherePreconditions.checkState(!connectionSession.getTransactionStatus().isInTransaction(), XATransactionNestedBeginException::new);
        ResponseHeader result = databaseProxyConnector.execute();
        TransactionRule transactionRule = metaData.getGlobalRuleMetaData().getSingleRule(TransactionRule.class);
        ShardingSphereTransactionManagerEngine engine = transactionRule.getResource();
        connectionSession.getConnectionContext().getTransactionContext().beginTransaction(transactionRule.getDefaultType().name(), engine.getTransactionManager(transactionRule.getDefaultType()));
        return result;
    }
}
