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

package org.apache.shardingsphere.infra.metadata.database.schema.reviser.table;

import org.apache.shardingsphere.infra.database.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(TableMetaDataReviseEngine.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TableMetadataReviseEngineTest<T extends ShardingSphereRule> {
    
    private final T mockRule = (T) mock(ShardingSphereRule.class);
    
    private final DatabaseType databaseType = mock(DatabaseType.class);
    
    private final DataSource dataSource = mock(DataSource.class);
    
    private final MetaDataReviseEntry metaDataReviseEntry = mock(MetaDataReviseEntry.class);
    
    @Test
    void assertGetRevisedTableName() {
        TableNameReviser tableNameReviser = mock(TableNameReviser.class);
        TableMetaData originalMetaData = new TableMetaData("originalTableName", new ArrayList<>(), null, null);
        TableMetaDataReviseEngine<T> tableMetaDataReviseEngine = new TableMetaDataReviseEngine<T>(mockRule, databaseType, dataSource, metaDataReviseEntry);
        doReturn(Optional.of(tableNameReviser)).when(metaDataReviseEntry).getTableNameReviser();
        when(tableNameReviser.revise(anyString(), eq(mockRule))).thenReturn("revisedTableName");
        TableMetaData revisedMetaData = tableMetaDataReviseEngine.revise(originalMetaData);
        assertThat(revisedMetaData.getName(), is("revisedTableName"));
    }
    
    @Test
    void assertGetOriginalTableName() {
        Collection<ColumnMetaData> columns = new ArrayList<>();
        columns.add(new ColumnMetaData("column1", 2, true, true, true, false, false, false));
        Collection<IndexMetaData> indexes = new ArrayList<>();
        indexes.add(new IndexMetaData("index1"));
        TableMetaData tableMetaData = new TableMetaData("originalTableName", columns, indexes, null);
        TableMetaDataReviseEngine<T> tableMetaDataReviseEngine = new TableMetaDataReviseEngine<T>(mockRule, databaseType, dataSource, metaDataReviseEntry);
        when(metaDataReviseEntry.getTableNameReviser()).thenReturn(Optional.empty());
        TableMetaData revisedMetaData = tableMetaDataReviseEngine.revise(tableMetaData);
        assertThat(revisedMetaData.getName(), is("originalTableName"));
    }
}
