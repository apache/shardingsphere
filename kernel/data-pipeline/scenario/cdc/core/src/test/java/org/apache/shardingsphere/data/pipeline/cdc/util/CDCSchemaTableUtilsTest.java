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
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
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

class CDCSchemaTableUtilsTest {
    
    @Test
    void assertParseTableExpression() {
        Map<String, ShardingSphereSchema> schemas = new HashMap<>(2, 1F);
        schemas.put("public", mockedPublicSchema());
        schemas.put("test", mockedTestSchema());
        ShardingSphereDatabase database = new ShardingSphereDatabase("sharding_db", TypedSPILoader.getService(DatabaseType.class, "openGauss"), null, null, schemas);
        List<SchemaTable> schemaTables = Arrays.asList(SchemaTable.newBuilder().setSchema("public").setTable("t_order").build(),
                SchemaTable.newBuilder().setSchema("test").setTable("*").build());
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
        Map<String, ShardingSphereTable> tables = new HashMap<>(2, 1F);
        tables.put("t_order", mock(ShardingSphereTable.class));
        tables.put("t_order2", mock(ShardingSphereTable.class));
        return new ShardingSphereSchema(tables, Collections.emptyMap());
    }
    
    private ShardingSphereSchema mockedTestSchema() {
        Map<String, ShardingSphereTable> tables = new HashMap<>(2, 1F);
        tables.put("t_order_item", mock(ShardingSphereTable.class));
        tables.put("t_order_item2", mock(ShardingSphereTable.class));
        return new ShardingSphereSchema(tables, Collections.emptyMap());
    }
    
    @Test
    void assertParseTableExpressionWithoutSchema() {
        Map<String, ShardingSphereSchema> schemas = Collections.singletonMap("sharding_db", mockedPublicSchema());
        ShardingSphereDatabase database = new ShardingSphereDatabase("sharding_db", TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), null, null, schemas);
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
