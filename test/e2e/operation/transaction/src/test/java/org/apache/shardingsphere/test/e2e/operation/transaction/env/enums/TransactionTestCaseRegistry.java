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

package org.apache.shardingsphere.test.e2e.operation.transaction.env.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Adapter;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionBaseE2EIT;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.database.mysql.MySQLJdbcTransactionE2ETT;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.database.mysql.MySQLProxyTransactionE2EIT;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.database.opengauss.OpenGaussJdbcTransactionE2EIT;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.database.opengauss.OpenGaussProxyTransactionE2EIT;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.database.postgresql.PostgreSQLJdbcTransactionE2EIT;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.database.postgresql.PostgreSQLProxyTransactionE2EIT;

@RequiredArgsConstructor
@Getter
public enum TransactionTestCaseRegistry {
    
    MYSQL_JDBC_IT(MySQLJdbcTransactionE2ETT.class, TypedSPILoader.getService(DatabaseType.class, "MySQL"), Adapter.JDBC.getValue()),
    
    MYSQL_PROXY_IT(MySQLProxyTransactionE2EIT.class, TypedSPILoader.getService(DatabaseType.class, "MySQL"), Adapter.PROXY.getValue()),
    
    OPENGAUSS_JDBC_IT(OpenGaussJdbcTransactionE2EIT.class, TypedSPILoader.getService(DatabaseType.class, "openGauss"), Adapter.JDBC.getValue()),
    
    OPENGAUSS_PROXY_IT(OpenGaussProxyTransactionE2EIT.class, TypedSPILoader.getService(DatabaseType.class, "openGauss"), Adapter.PROXY.getValue()),
    
    POSTGRESQL_JDBC_IT(PostgreSQLJdbcTransactionE2EIT.class, TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"), Adapter.JDBC.getValue()),
    
    POSTGRESQL_PROXY_IT(PostgreSQLProxyTransactionE2EIT.class, TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"), Adapter.PROXY.getValue());
    
    private final Class<? extends TransactionBaseE2EIT> testCaseClass;
    
    private final DatabaseType databaseType;
    
    private final String runningAdaptor;
}
