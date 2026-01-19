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

package org.apache.shardingsphere.infra.metadata.database.schema.reviser.column;

import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColumnReviseEngineTest {
    
    @Mock
    private ShardingSphereRule rule;
    
    @Mock
    private MetaDataReviseEntry<ShardingSphereRule> reviseEntry;
    
    @Test
    void assertRevise() {
        String tableName = "foo_tbl";
        when(reviseEntry.getColumnExistedReviser(rule, tableName)).thenReturn(Optional.empty());
        when(reviseEntry.getColumnNameReviser(rule, tableName)).thenReturn(Optional.empty());
        when(reviseEntry.getColumnGeneratedReviser(rule, tableName)).thenReturn(Optional.empty());
        ColumnMetaData columnMetaData = new ColumnMetaData("foo_col", 1, true, false, "", true, false, false, false);
        Collection<ColumnMetaData> actual = new ColumnReviseEngine<>(rule, reviseEntry).revise(tableName, Collections.singleton(columnMetaData));
        assertThat(actual.size(), is(1));
        ColumnMetaData revisedColumn = actual.iterator().next();
        assertThat(revisedColumn.getName(), is("foo_col"));
        assertThat(revisedColumn.getDataType(), is(1));
        assertTrue(revisedColumn.isPrimaryKey());
        assertFalse(revisedColumn.isGenerated());
        assertTrue(revisedColumn.isCaseSensitive());
        assertFalse(revisedColumn.isVisible());
        assertFalse(revisedColumn.isUnsigned());
        assertFalse(revisedColumn.isNullable());
    }
}
