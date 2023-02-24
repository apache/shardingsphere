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

package org.apache.shardingsphere.test.e2e.transaction.env.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionBaseE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.test.e2e.transaction.engine.database.mysql.MySQLJdbcTransactionE2ETT;
import org.apache.shardingsphere.test.e2e.transaction.engine.database.mysql.MySQLProxyTransactionE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.database.opengauss.OpenGaussJdbcTransactionE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.database.opengauss.OpenGaussProxyTransactionE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.database.postgresql.PostgreSQLJdbcTransactionE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.database.postgresql.PostgreSQLProxyTransactionE2EIT;

@RequiredArgsConstructor
@Getter
public enum TransactionTestCaseRegistry {
    
    MySQL_JDBC_IT(MySQLJdbcTransactionE2ETT.class, TransactionTestConstants.MYSQL, AdapterType.JDBC.getValue()),
    
    MySQL_PROXY_IT(MySQLProxyTransactionE2EIT.class, TransactionTestConstants.MYSQL, AdapterType.PROXY.getValue()),
    
    OPENGAUSS_JDBC_IT(OpenGaussJdbcTransactionE2EIT.class, TransactionTestConstants.OPENGAUSS, AdapterType.JDBC.getValue()),
    
    OPENGAUSS_PROXY_IT(OpenGaussProxyTransactionE2EIT.class, TransactionTestConstants.OPENGAUSS, AdapterType.PROXY.getValue()),
    
    POSTGRESQL_JDBC_IT(PostgreSQLJdbcTransactionE2EIT.class, TransactionTestConstants.POSTGRESQL, AdapterType.JDBC.getValue()),
    
    POSTGRESQL_PROXY_IT(PostgreSQLProxyTransactionE2EIT.class, TransactionTestConstants.POSTGRESQL, AdapterType.PROXY.getValue());
    
    private final Class<? extends TransactionBaseE2EIT> testCaseClass;
    
    private final String dbType;
    
    private final String runningAdaptor;
}
