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

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MySQLComStmtPrepareParameterMarkerExtractorTest {
    
    @Test
    void assertFindColumnsOfParameterMarkersForInsertStatement() {
        String sql = "insert into user (id, name, age) values (1, ?, ?), (?, 'bar', ?)";
        SQLStatement sqlStatement = new ShardingSphereSQLParserEngine(TypedSPILoader.getService(DatabaseType.class, "MySQL"), new CacheOption(0, 0), new CacheOption(0, 0), false).parse(sql, false);
        ShardingSphereSchema schema = prepareSchema();
        Map<ParameterMarkerSegment, ShardingSphereColumn> actual = MySQLComStmtPrepareParameterMarkerExtractor.findColumnsOfParameterMarkers(sqlStatement, schema);
        List<ParameterMarkerSegment> parameterMarkerSegments = new ArrayList<>(((AbstractSQLStatement) sqlStatement).getParameterMarkerSegments());
        assertThat(actual.get(parameterMarkerSegments.get(0)), is(schema.getTable("user").getColumn("name")));
        assertThat(actual.get(parameterMarkerSegments.get(1)), is(schema.getTable("user").getColumn("age")));
        assertThat(actual.get(parameterMarkerSegments.get(2)), is(schema.getTable("user").getColumn("id")));
        assertThat(actual.get(parameterMarkerSegments.get(3)), is(schema.getTable("user").getColumn("age")));
    }
    
    private ShardingSphereSchema prepareSchema() {
        ShardingSphereTable table = new ShardingSphereTable();
        table.putColumn(new ShardingSphereColumn("id", Types.BIGINT, true, false, false, false, true));
        table.putColumn(new ShardingSphereColumn("name", Types.VARCHAR, false, false, false, false, false));
        table.putColumn(new ShardingSphereColumn("age", Types.SMALLINT, false, false, false, false, true));
        ShardingSphereSchema result = new ShardingSphereSchema();
        result.getTables().put("user", table);
        return result;
    }
}
