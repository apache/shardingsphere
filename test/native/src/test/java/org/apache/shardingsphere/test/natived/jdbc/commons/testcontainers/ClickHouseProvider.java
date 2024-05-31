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

package org.apache.shardingsphere.test.natived.jdbc.commons.testcontainers;

import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.JdbcDatabaseContainerProvider;
import org.testcontainers.utility.DockerImageName;

/**
 * TODO Initialize a new JdbcDatabaseContainerProvider implementation due to
 *  <a href="https://github.com/testcontainers/testcontainers-java/issues/8736">testcontainers/testcontainers-java#8736</a> not closed.
 */
public final class ClickHouseProvider extends JdbcDatabaseContainerProvider {
    
    /**
     * The reason for using `0` as the separator is that the URL_MATCHING_PATTERN of the {@link org.testcontainers.jdbc.ConnectionUrl.Patterns}
     * sets {@code "(?<databaseType>[a-z0-9]+)"} for the `databaseType` part.
     *
     * @param databaseType {@link String}
     * @return <code>true</code> when provider can handle this database type, else <code>false</code>.
     */
    @Override
    public boolean supports(final String databaseType) {
        return "shardingsphere0clickhouse".equals(databaseType);
    }
    
    @Override
    public JdbcDatabaseContainer<?> newInstance(final String tag) {
        return new ClickHouseContainer(DockerImageName.parse("clickhouse/clickhouse-server").withTag(tag));
    }
}
