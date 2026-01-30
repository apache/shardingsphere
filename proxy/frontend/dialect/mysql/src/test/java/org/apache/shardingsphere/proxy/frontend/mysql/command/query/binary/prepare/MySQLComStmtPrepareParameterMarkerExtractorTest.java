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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MySQLComStmtPrepareParameterMarkerExtractorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertFindColumnsOfParameterMarkersForInsertStatement() {
        String sql = "INSERT INTO user (id, name, age) VALUES (1, ?, ?), (?, 'bar', ?)";
        SQLStatement sqlStatement = new ShardingSphereSQLParserEngine(databaseType, new CacheOption(0, 0L), new CacheOption(0, 0L)).parse(sql, false);
        ShardingSphereSchema schema = createSchema();
        List<ShardingSphereColumn> actual = MySQLComStmtPrepareParameterMarkerExtractor.findColumnsOfParameterMarkers(sqlStatement, schema);
        assertThat(actual.get(0), is(schema.getTable("user").getColumn("name")));
        assertThat(actual.get(1), is(schema.getTable("user").getColumn("age")));
        assertThat(actual.get(2), is(schema.getTable("user").getColumn("id")));
        assertThat(actual.get(3), is(schema.getTable("user").getColumn("age")));
    }
    
    private ShardingSphereSchema createSchema() {
        ShardingSphereTable table = new ShardingSphereTable("user", Arrays.asList(
                new ShardingSphereColumn("id", Types.BIGINT, true, false, false, false, true, false),
                new ShardingSphereColumn("name", Types.VARCHAR, false, false, false, false, false, false),
                new ShardingSphereColumn("age", Types.SMALLINT, false, false, false, false, true, false)), Collections.emptyList(), Collections.emptyList());
        return new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList());
    }
}
