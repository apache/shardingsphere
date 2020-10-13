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

package org.apache.shardingsphere.infra.metadata.model.physical.jdbc.handler;

import java.util.Optional;
import java.util.Properties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class OracleTableNamePatternHandlerTest {
    
    private static final String TABLE_NAME_PATTERN = "t_order_0";
    
    private final DatabaseType oracleDatabaseType = DatabaseTypes.getTrunkDatabaseType("Oracle");
    
    private final DatabaseType mysqlDatabaseType = DatabaseTypes.getTrunkDatabaseType("MySQL");
    
    @Test
    public void assertDecorateTableNamePattern() {
        ShardingSphereServiceLoader.register(TableNamePatternHandler.class);
        Optional<TableNamePatternHandler> oracleTableNamePatternHandler = findTableNamePatternHandler(oracleDatabaseType);
        assertThat(oracleTableNamePatternHandler.isPresent(), is(true));
        Optional<TableNamePatternHandler> mysqlTableNamePatternHandler = findTableNamePatternHandler(mysqlDatabaseType);
        assertThat(mysqlTableNamePatternHandler.isPresent(), is(false));
        String oracleResult = oracleTableNamePatternHandler.map(handler -> handler.decorate(TABLE_NAME_PATTERN)).orElse(TABLE_NAME_PATTERN);
        assertEquals(oracleResult, "T_ORDER_0");
        String mysqlResult = mysqlTableNamePatternHandler.map(handler -> handler.decorate(TABLE_NAME_PATTERN)).orElse(TABLE_NAME_PATTERN);
        assertEquals(mysqlResult, TABLE_NAME_PATTERN);
    }
    
    private Optional<TableNamePatternHandler> findTableNamePatternHandler(final DatabaseType databaseType) {
        try {
            return Optional.of(TypedSPIRegistry.getRegisteredService(TableNamePatternHandler.class, databaseType.getName(), new Properties()));
        } catch (final ServiceProviderNotFoundException ignored) {
            return Optional.empty();
        }
    }
}
