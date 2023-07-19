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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.provider;

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.Scope;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariable;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TransactionIsolationValueProviderTest {
    
    @Test
    void assertGetGlobalValue() {
        assertThat(new TransactionIsolationValueProvider().get(Scope.GLOBAL, null, MySQLSystemVariable.TRANSACTION_ISOLATION), is("REPEATABLE-READ"));
        assertThat(new TransactionIsolationValueProvider().get(Scope.GLOBAL, null, MySQLSystemVariable.TX_ISOLATION), is("REPEATABLE-READ"));
    }
    
    @Test
    void assertGetSessionValue() {
        ConnectionSession connectionSession = new ConnectionSession(new MySQLDatabaseType(), TransactionType.LOCAL, new DefaultAttributeMap());
        assertThat(new TransactionIsolationValueProvider().get(Scope.SESSION, connectionSession, MySQLSystemVariable.TRANSACTION_ISOLATION), is("REPEATABLE-READ"));
        assertThat(new TransactionIsolationValueProvider().get(Scope.SESSION, connectionSession, MySQLSystemVariable.TX_ISOLATION), is("REPEATABLE-READ"));
        connectionSession.setIsolationLevel(TransactionIsolationLevel.READ_COMMITTED);
        assertThat(new TransactionIsolationValueProvider().get(Scope.SESSION, connectionSession, MySQLSystemVariable.TRANSACTION_ISOLATION), is("READ-COMMITTED"));
        assertThat(new TransactionIsolationValueProvider().get(Scope.SESSION, connectionSession, MySQLSystemVariable.TX_ISOLATION), is("READ-COMMITTED"));
    }
}
