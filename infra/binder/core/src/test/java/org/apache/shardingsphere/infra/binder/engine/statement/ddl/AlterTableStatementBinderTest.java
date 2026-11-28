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

package org.apache.shardingsphere.infra.binder.engine.statement.ddl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.partition.ExchangePartitionDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(SimpleTableSegmentBinder.class)
class AlterTableStatementBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @ParameterizedTest(name = "partition={0}")
    @ValueSource(strings = {"NONE", "NAMED", "VALUE"})
    void assertBindPreservesExchangePartition(final String scenario) {
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(12, 18, new IdentifierValue("t_order")));
        PartitionSegment partition = "NAMED".equals(scenario) ? new PartitionSegment(39, 40, new IdentifierValue("p0")) : null;
        SimpleTableSegment exchangeTable = new SimpleTableSegment(new TableNameSegment(53, 63, new IdentifierValue("t_order_old")));
        Collection<ExchangePartitionDefinitionSegment> expectedDefinitions = "NONE".equals(scenario) ? Collections.emptyList()
                : Collections.singletonList(new ExchangePartitionDefinitionSegment(20, 79, partition, exchangeTable));
        AlterTableStatement statement = AlterTableStatement.builder().databaseType(databaseType).table(table).exchangePartitionDefinitions(expectedDefinitions).build();
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        when(SimpleTableSegmentBinder.bind(eq(table), same(context), any())).thenReturn(table);
        AlterTableStatement actual = new AlterTableStatementBinder().bind(statement, context);
        assertThat(actual.getExchangePartitionDefinitions(), is(expectedDefinitions));
    }
}
