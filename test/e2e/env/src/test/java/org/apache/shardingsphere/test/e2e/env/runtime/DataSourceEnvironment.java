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

package org.apache.shardingsphere.test.e2e.env.runtime;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;

/**
 * Data source environment.
 */
public final class DataSourceEnvironment {
    
    /**
     * Get driver class name.
     *
     * @param databaseType database type
     * @return driver class name
     * @throws UnsupportedOperationException unsupported operation exception
     */
    public static String getDriverClassName(final DatabaseType databaseType) {
        switch (databaseType.getType()) {
            case "H2":
                return "org.h2.Driver";
            case "MySQL":
                return "com.mysql.cj.jdbc.Driver";
            case "MariaDB":
                return "org.mariadb.jdbc.Driver";
            case "PostgreSQL":
                return "org.postgresql.Driver";
            case "openGauss":
                return "org.opengauss.Driver";
            default:
                throw new UnsupportedOperationException(databaseType.getType());
        }
    }
    
    /**
     * Get URL.
     *
     * @param databaseType database type
     * @param host database host
     * @param port database port
     * @return URL
     * @throws UnsupportedOperationException unsupported operation exception
     */
    public static String getURL(final DatabaseType databaseType, final String host, final int port) {
        switch (databaseType.getType()) {
            case "H2":
                return "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL;USER=root;PASSWORD=Root@123";
            case "MySQL":
                return String.format("jdbc:mysql://%s:%s?useSSL=true&requireSSL=true&enabledTLSProtocols=TLSv1.2,TLSv1.3&verifyServerCertificate=false"
                        + "&useServerPrepStmts=true&useLocalSessionState=true&characterEncoding=utf-8&allowMultiQueries=true&rewriteBatchedStatements=true", host, port);
            case "MariaDB":
                return String.format("jdbc:mariadb://%s:%s?useSSL=false&useServerPrepStmts=true&useLocalSessionState=true&characterEncoding=utf-8&allowMultiQueries=true&rewriteBatchedStatements=true",
                        host, port);
            case "PostgreSQL":
                return String.format("jdbc:postgresql://%s:%s/?ssl=on&sslmode=prefer", host, port);
            case "openGauss":
                return String.format("jdbc:opengauss://%s:%s/", host, port);
            default:
                throw new UnsupportedOperationException(databaseType.getType());
        }
    }
    
    /**
     * Get URL.
     *
     * @param databaseType database type
     * @param host database host
     * @param port database port
     * @param dataSourceName data source name
     * @return URL
     * @throws UnsupportedOperationException unsupported operation exception
     */
    public static String getURL(final DatabaseType databaseType, final String host, final int port, final String dataSourceName) {
        switch (databaseType.getType()) {
            case "H2":
                return String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL;USER=root;PASSWORD=Root@123", dataSourceName);
            case "MySQL":
                return String.format(
                        "jdbc:mysql://%s:%s/%s?useSSL=true&requireSSL=true&enabledTLSProtocols=TLSv1.2,TLSv1.3&verifyServerCertificate=false"
                                + "&useServerPrepStmts=true&useLocalSessionState=true&characterEncoding=utf-8&allowPublicKeyRetrieval=true&allowMultiQueries=true&rewriteBatchedStatements=true",
                        host, port, dataSourceName);
            case "MariaDB":
                return String.format("jdbc:mariadb://%s:%s/%s?useSSL=false&useServerPrepStmts=true&useLocalSessionState=true&characterEncoding=utf-8&allowMultiQueries=true"
                        + "&rewriteBatchedStatements=true", host, port, dataSourceName);
            case "PostgreSQL":
                return String.format("jdbc:postgresql://%s:%s/%s?ssl=on&sslmode=prefer", host, port, dataSourceName);
            case "openGauss":
                return String.format("jdbc:opengauss://%s:%s/%s?batchMode=OFF", host, port, dataSourceName);
            default:
                throw new UnsupportedOperationException(databaseType.getType());
        }
    }
}
