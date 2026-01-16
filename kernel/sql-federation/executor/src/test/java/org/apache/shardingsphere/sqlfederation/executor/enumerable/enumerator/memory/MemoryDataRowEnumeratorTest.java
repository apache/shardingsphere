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

package org.apache.shardingsphere.sqlfederation.executor.enumerable.enumerator.memory;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.statistics.RowStatistics;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryDataRowEnumeratorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertIterateAndResetState() {
        Collection<ShardingSphereColumn> columns = Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, "int", false, true, false, true),
                new ShardingSphereColumn("name", Types.VARCHAR, false, false, "varchar", false, true, false, true));
        Collection<RowStatistics> rows = Arrays.asList(
                new RowStatistics(Arrays.asList(1, "foo_name")),
                new RowStatistics(Arrays.asList(2, "bar_name")));
        MemoryDataRowEnumerator enumerator = new MemoryDataRowEnumerator(rows, columns, databaseType);
        assertNull(enumerator.current());
        assertTrue(enumerator.moveNext());
        Object firstRow = enumerator.current();
        assertThat(firstRow, instanceOf(Object[].class));
        assertThat((Object[]) firstRow, arrayContaining(1, "foo_name"));
        assertTrue(enumerator.moveNext());
        assertThat((Object[]) enumerator.current(), arrayContaining(2, "bar_name"));
        assertFalse(enumerator.moveNext());
        assertNull(enumerator.current());
        assertTrue(enumerator.moveNext());
        assertThat((Object[]) enumerator.current(), arrayContaining(1, "foo_name"));
        enumerator.reset();
        enumerator.close();
        assertNull(enumerator.current());
    }
}
