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

package org.apache.shardingsphere.infra.metadata.database.schema.util;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class IndexMetaDataUtilsTest {
    
    @Test
    void assertGetLogicIndexNameWithIndexNameSuffix() {
        assertThat(IndexMetaDataUtils.getLogicIndexName("order_index_t_order", "t_order"), is("order_index"));
    }
    
    @Test
    void assertGetLogicIndexNameWithMultiIndexNameSuffix() {
        assertThat(IndexMetaDataUtils.getLogicIndexName("order_t_order_index_t_order", "t_order"), is("order_t_order_index"));
    }
    
    @Test
    void assertGetLogicIndexNameWithoutIndexNameSuffix() {
        assertThat(IndexMetaDataUtils.getLogicIndexName("order_index", "t_order"), is("order_index"));
    }
    
    @Test
    void assertGetActualIndexNameWithActualTableName() {
        assertThat(IndexMetaDataUtils.getActualIndexName("order_index", "t_order"), is("order_index_t_order"));
    }
    
    @Test
    void assertGetActualIndexNameWithoutActualTableName() {
        assertThat(IndexMetaDataUtils.getActualIndexName("order_index", null), is("order_index"));
    }
    
    @Test
    void assertGetTableNames() {
        IndexSegment indexSegment = new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("foo_idx")));
        Collection<QualifiedTable> actual = IndexMetaDataUtils.getTableNames(buildDatabase(), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), Collections.singleton(indexSegment));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getSchemaName(), is("foo_db"));
        assertThat(actual.iterator().next().getTableName(), is("foo_tbl"));
    }
    
    private ShardingSphereDatabase buildDatabase() {
        ShardingSphereTable table = new ShardingSphereTable(
                "foo_tbl", Collections.emptyList(), Collections.singleton(new ShardingSphereIndex("foo_idx", Collections.emptyList(), false)), Collections.emptyList());
        Collection<ShardingSphereSchema> schemas = Collections.singleton(new ShardingSphereSchema("foo_db", mock(DatabaseType.class), Collections.singleton(table), Collections.emptyList()));
        return new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), mock(ResourceMetaData.class), mock(RuleMetaData.class), schemas);
    }
}
