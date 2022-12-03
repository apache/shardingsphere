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

package org.apache.shardingsphere.integration.transaction.env.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.integration.transaction.engine.mysql.MysqlJdbcTransactionIT;
import org.apache.shardingsphere.integration.transaction.engine.mysql.MysqlProxyTransactionIT;
import org.apache.shardingsphere.integration.transaction.engine.opengauss.OpenGaussJdbcTransactionIT;
import org.apache.shardingsphere.integration.transaction.engine.opengauss.OpenGaussProxyTransactionIT;
import org.apache.shardingsphere.integration.transaction.engine.postgresql.PostgresqlJdbcTransactionIT;
import org.apache.shardingsphere.integration.transaction.engine.postgresql.PostgresqlProxyTransactionIT;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.AdapterContainerConstants;

/**
 * Transaction test case registry.
 */
@RequiredArgsConstructor
@Getter
public enum TransactionTestCaseRegistry {
    
    MySQL_JDBC_IT(MysqlJdbcTransactionIT.class, TransactionTestConstants.MYSQL, AdapterContainerConstants.JDBC),
    
    MySQL_PROXY_IT(MysqlProxyTransactionIT.class, TransactionTestConstants.MYSQL, AdapterContainerConstants.PROXY),
    
    OPENGAUSS_JDBC_IT(OpenGaussJdbcTransactionIT.class, TransactionTestConstants.OPENGAUSS, AdapterContainerConstants.JDBC),
    
    OPENGAUSS_PROXY_IT(OpenGaussProxyTransactionIT.class, TransactionTestConstants.OPENGAUSS, AdapterContainerConstants.PROXY),
    
    POSTGRESQL_JDBC_IT(PostgresqlJdbcTransactionIT.class, TransactionTestConstants.POSTGRESQL, AdapterContainerConstants.JDBC),
    
    POSTGRESQL_PROXY_IT(PostgresqlProxyTransactionIT.class, TransactionTestConstants.POSTGRESQL, AdapterContainerConstants.PROXY);
    
    private final Class<? extends BaseTransactionITCase> testCaseClass;
    
    private final String dbType;
    
    private final String runningAdaptor;
}
