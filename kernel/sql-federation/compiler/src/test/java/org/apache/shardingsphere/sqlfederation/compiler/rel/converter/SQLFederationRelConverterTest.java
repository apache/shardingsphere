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

package org.apache.shardingsphere.sqlfederation.compiler.rel.converter;

import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class SQLFederationRelConverterTest {
    
    @Mock
    private SQLParserRule sqlParserRule;
    
    private CalciteSchema calciteSchema;
    
    private SQLFederationRelConverter converter;
    
    private SqlNode sqlNode;
    
    @BeforeEach
    void setUp() throws SqlParseException {
        calciteSchema = CalciteSchema.createRootSchema(true);
        CompilerContext compilerContext = new CompilerContext(sqlParserRule, calciteSchema, new CalciteConnectionConfigImpl(new Properties()), Collections.singleton(SqlStdOperatorTable.instance()));
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        converter = new SQLFederationRelConverter(compilerContext, Collections.singletonList("public"), databaseType, EnumerableConvention.INSTANCE);
        sqlNode = SqlParser.create("SELECT 1").parseQuery();
    }
    
    @Test
    void assertGetSchemaPlus() {
        assertThat(converter.getSchemaPlus().getName(), is(calciteSchema.plus().getName()));
    }
    
    @Test
    void assertConvertQuery() {
        RelRoot relRoot = converter.convertQuery(sqlNode, true, true);
        assertNotNull(relRoot.rel);
        assertNotNull(relRoot.validatedRowType);
    }
    
    @Test
    void assertGetValidatedNodeType() {
        converter.convertQuery(sqlNode, true, true);
        RelDataType validatedType = converter.getValidatedNodeType(sqlNode);
        assertThat(validatedType.getFieldCount(), is(1));
    }
    
    @Test
    void assertGetCluster() {
        assertNotNull(converter.getCluster().getPlanner());
    }
}
