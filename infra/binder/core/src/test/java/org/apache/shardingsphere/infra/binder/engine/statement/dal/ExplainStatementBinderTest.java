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

package org.apache.shardingsphere.infra.binder.engine.statement.dal;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class ExplainStatementBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Mock
    private ShardingSphereTable table;
    
    @Test
    void assertBindWithDMLStatement() {
        DMLStatement dmlStatement = new TestDMLStatement(databaseType);
        ExplainStatement original = new ExplainStatement(databaseType, dmlStatement);
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.setSkipMetadataValidate(true);
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(metaData, "foo_db", hintValueContext, original);
        ExplainStatement actual = new ExplainStatementBinder().bind(original, binderContext);
        assertNotSame(original, actual);
        assertThat(actual.getExplainableSQLStatement().getClass(), is(TestDMLStatement.class));
        assertSame(dmlStatement, actual.getExplainableSQLStatement());
    }
    
    @Test
    void assertBindWithNonDMLStatement() {
        SQLStatement nonDMLStatement = new TestNonDMLStatement(databaseType);
        ExplainStatement original = new ExplainStatement(databaseType, nonDMLStatement);
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.setSkipMetadataValidate(true);
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(metaData, "foo_db", hintValueContext, original);
        ExplainStatement actual = new ExplainStatementBinder().bind(original, binderContext);
        assertNotSame(original, actual);
        assertSame(nonDMLStatement, actual.getExplainableSQLStatement());
    }
    
    private static final class TestDMLStatement extends DMLStatement {
        
        TestDMLStatement(final DatabaseType databaseType) {
            super(databaseType);
        }
    }
    
    private static final class TestNonDMLStatement extends DALStatement {
        
        TestNonDMLStatement(final DatabaseType databaseType) {
            super(databaseType);
        }
    }
}
