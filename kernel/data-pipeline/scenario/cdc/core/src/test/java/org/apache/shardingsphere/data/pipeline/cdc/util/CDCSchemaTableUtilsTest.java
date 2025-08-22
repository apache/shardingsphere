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

package org.apache.shardingsphere.data.pipeline.cdc.util;

import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CDCSchemaTableUtilsTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    @Test
    void assertParseTableExpression() {
        ShardingSphereDatabase database = new ShardingSphereDatabase("sharding_db", databaseType, null, null, Arrays.asList(mockedPublicSchema(), mockedTestSchema()));
        List<SchemaTable> schemaTables = Arrays.asList(SchemaTable.newBuilder().setSchema("public").setTable("t_order").build(), SchemaTable.newBuilder().setSchema("test").setTable("*").build());
        Map<String, Set<String>> expected = new HashMap<>(2, 1F);
        expected.put("test", new HashSet<>(Arrays.asList("t_order_item", "t_order_item2")));
        expected.put("public", Collections.singleton("t_order"));
        Map<String, Set<String>> actual = CDCSchemaTableUtils.parseTableExpressionWithSchema(database, schemaTables);
        assertThat(actual, is(expected));
        schemaTables = Collections.singletonList(SchemaTable.newBuilder().setTable("t_order").build());
        actual = CDCSchemaTableUtils.parseTableExpressionWithSchema(database, schemaTables);
        expected = Collections.singletonMap("public", Collections.singleton("t_order"));
        assertThat(actual, is(expected));
        schemaTables = Collections.singletonList(SchemaTable.newBuilder().setSchema("*").setTable("t_order").build());
        actual = CDCSchemaTableUtils.parseTableExpressionWithSchema(database, schemaTables);
        expected = Collections.singletonMap("public", Collections.singleton("t_order"));
        assertThat(actual, is(expected));
    }
    
    private ShardingSphereSchema mockedPublicSchema() {
        ShardingSphereTable table1 = mock(ShardingSphereTable.class);
        when(table1.getName()).thenReturn("t_order");
        ShardingSphereTable table2 = mock(ShardingSphereTable.class);
        when(table2.getName()).thenReturn("t_order2");
        return new ShardingSphereSchema("public", Arrays.asList(table1, table2), Collections.emptyList());
    }
    
    private ShardingSphereSchema mockedTestSchema() {
        ShardingSphereTable table1 = mock(ShardingSphereTable.class);
        when(table1.getName()).thenReturn("t_order_item");
        ShardingSphereTable table2 = mock(ShardingSphereTable.class);
        when(table2.getName()).thenReturn("t_order_item2");
        return new ShardingSphereSchema("test", Arrays.asList(table1, table2), Collections.emptyList());
    }
    
    @Test
    void assertParseTableExpressionWithoutSchema() {
        ShardingSphereDatabase database = new ShardingSphereDatabase("public", TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), null, null, Collections.singleton(mockedPublicSchema()));
        List<String> schemaTables = Collections.singletonList("*");
        Collection<String> actualWildcardTable = CDCSchemaTableUtils.parseTableExpressionWithoutSchema(database, schemaTables);
        Set<String> expectedWildcardTable = new HashSet<>(Arrays.asList("t_order", "t_order2"));
        assertThat(actualWildcardTable, is(expectedWildcardTable));
        schemaTables = Collections.singletonList("t_order");
        Collection<String> actualSingleTable = CDCSchemaTableUtils.parseTableExpressionWithoutSchema(database, schemaTables);
        Set<String> expectedSingleTable = new HashSet<>(Collections.singletonList("t_order"));
        assertThat(actualSingleTable, is(expectedSingleTable));
    }
}
