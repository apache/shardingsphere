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

package org.apache.shardingsphere.sqlfederation.compiler.metadata.schema;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class SQLFederationDatabaseTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertNew() {
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", Collections.singleton(createTable()), Collections.emptyList(), databaseType);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class), new RuleMetaData(Collections.emptyList()), Collections.singleton(schema));
        SQLFederationDatabase actual = new SQLFederationDatabase(database, databaseType);
        assertThat(actual.getName(), is("foo_db"));
        assertNotNull(actual.getSubSchemaMap().get("foo_schema"));
    }
    
    private ShardingSphereTable createTable() {
        ShardingSphereColumn column = new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, true);
        return new ShardingSphereTable("foo_tbl", Collections.singletonList(column), Collections.emptyList(), Collections.emptyList());
    }
}
