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

package org.apache.shardingsphere.test.e2e.discovery.cases;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.discovery.cases.mysql.env.MySQLMGREnvironment;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * Database cluster environment factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseClusterEnvironmentFactory {
    
    /**
     * Create new instance of Database cluster environment.
     *
     * @param environment environment
     * @param dataSources storage data sources
     * @return Database cluster environment instance
     * @throws SQLException SQL exception
     */
    public static DatabaseClusterEnvironment newInstance(final String environment, final List<DataSource> dataSources) throws SQLException {
        if ("MySQL.MGR".equals(environment)) {
            return new MySQLMGREnvironment(dataSources);
        }
        throw new UnsupportedOperationException("Unsupported database environment : " + environment);
    }
}
