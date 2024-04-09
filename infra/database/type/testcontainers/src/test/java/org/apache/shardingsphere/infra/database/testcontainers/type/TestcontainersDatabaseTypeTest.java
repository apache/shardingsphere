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

package org.apache.shardingsphere.infra.database.testcontainers.type;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TestcontainersDatabaseTypeTest {
    
    @Test
    void assertGetJdbcUrlPrefixes() {
        assertThat(TypedSPILoader.getService(DatabaseType.class, "TC-ClickHouse").getJdbcUrlPrefixes(), is(Collections.singleton("jdbc:tc:clickhouse:")));
        assertThat(TypedSPILoader.getService(DatabaseType.class, "TC-MariaDB").getJdbcUrlPrefixes(), is(Collections.singleton("jdbc:tc:mariadb:")));
        assertThat(TypedSPILoader.getService(DatabaseType.class, "TC-MySQL").getJdbcUrlPrefixes(), is(Collections.singleton("jdbc:tc:mysql:")));
        assertThat(TypedSPILoader.getService(DatabaseType.class, "TC-Oracle").getJdbcUrlPrefixes(), is(Collections.singleton("jdbc:tc:oracle:")));
        assertThat(TypedSPILoader.getService(DatabaseType.class, "TC-PostgreSQL").getJdbcUrlPrefixes(), is(Collections.singleton("jdbc:tc:postgresql:")));
        assertThat(TypedSPILoader.getService(DatabaseType.class, "TC-SQLServer").getJdbcUrlPrefixes(), is(Collections.singleton("jdbc:tc:sqlserver:")));
        assertThat(TypedSPILoader.getService(DatabaseType.class, "TC-TiDB").getJdbcUrlPrefixes(), is(Collections.singleton("jdbc:tc:tidb:")));
    }
}
