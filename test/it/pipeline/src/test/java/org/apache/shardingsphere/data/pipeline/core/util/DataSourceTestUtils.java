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

package org.apache.shardingsphere.data.pipeline.core.util;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class DataSourceTestUtils {
    
    /**
     * Create Hikari data source.
     *
     * @param databaseName database name
     * @return Hikari data source
     */
    public static HikariDataSource createHikariDataSource(final String databaseName) {
        HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl(String.format("jdbc:h2:mem:%s;DATABASE_TO_UPPER=false;MODE=MySQL", databaseName));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(10);
        result.setMinimumIdle(2);
        result.setConnectionTimeout(15L * 1000L);
        result.setIdleTimeout(40L * 1000L);
        return result;
    }
}
