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

package org.apache.shardingsphere.sqlfederation.compiler.metadata.view;

import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.SQLNodeConverterEngine;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(SQLNodeConverterEngine.class)
class ShardingSphereViewExpanderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertExpandView() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        SQLParserEngine sqlParserEngine = mock(SQLParserEngine.class);
        when(sqlParserEngine.parse("SELECT * FROM foo_tbl", false)).thenReturn(sqlStatement);
        SQLParserRule sqlParserRule = mock(SQLParserRule.class);
        when(sqlParserRule.getSQLParserEngine(databaseType)).thenReturn(sqlParserEngine);
        SqlNode sqlNode = mock(SqlNode.class);
        when(SQLNodeConverterEngine.convert(sqlStatement)).thenReturn(sqlNode);
        SqlToRelConverter sqlToRelConverter = mock(SqlToRelConverter.class);
        RelRoot expectedRelRoot = mock(RelRoot.class);
        when(sqlToRelConverter.convertQuery(sqlNode, true, true)).thenReturn(expectedRelRoot);
        ShardingSphereViewExpander expander = new ShardingSphereViewExpander(sqlParserRule, databaseType, sqlToRelConverter);
        assertThat(expander.expandView(mock(RelDataType.class), "SELECT * FROM foo_tbl", Collections.emptyList(), Collections.emptyList()), is(expectedRelRoot));
    }
}
