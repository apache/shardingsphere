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

package org.apache.shardingsphere.singletable.route.validator.ddl;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropSchemaStatement;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SingleTableDropSchemaMetadataValidatorTest {
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateWithoutCascadeSchema() {
        new SingleTableDropSchemaMetadataValidator().validate(mock(SingleTableRule.class, RETURNS_DEEP_STUBS), createSQLStatementContext("foo_schema", false), mockDatabase());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateWithNotExistedSchema() {
        ShardingSphereDatabase database = mockDatabase();
        when(database.getSchema("not_existed_schema")).thenReturn(null);
        new SingleTableDropSchemaMetadataValidator().validate(mock(SingleTableRule.class, RETURNS_DEEP_STUBS), createSQLStatementContext("not_existed_schema", true), database);
    }
    
    @Test
    public void assertValidate() {
        new SingleTableDropSchemaMetadataValidator().validate(mock(SingleTableRule.class, RETURNS_DEEP_STUBS), createSQLStatementContext("foo_schema", true), mockDatabase());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = new ShardingSphereSchema();
        schema.put("foo_table", new ShardingSphereTable());
        when(result.getSchemas()).thenReturn(Collections.singletonMap("foo_schema", schema));
        return result;
    }
    
    private SQLStatementContext<DropSchemaStatement> createSQLStatementContext(final String schemaName, final boolean isCascade) {
        PostgreSQLDropSchemaStatement dropSchemaStatement = mock(PostgreSQLDropSchemaStatement.class, RETURNS_DEEP_STUBS);
        when(dropSchemaStatement.isContainsCascade()).thenReturn(isCascade);
        when(dropSchemaStatement.getSchemaNames()).thenReturn(Collections.singleton(new IdentifierValue(schemaName)));
        return new CommonSQLStatementContext<>(dropSchemaStatement);
    }
}
