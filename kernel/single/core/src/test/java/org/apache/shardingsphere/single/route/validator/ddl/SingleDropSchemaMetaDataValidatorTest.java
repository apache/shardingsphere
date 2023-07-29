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

package org.apache.shardingsphere.single.route.validator.ddl;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.UnknownSQLStatementContext;
import org.apache.shardingsphere.infra.exception.SchemaNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.single.exception.DropNotEmptySchemaException;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropSchemaStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleDropSchemaMetaDataValidatorTest {
    
    @Test
    void assertValidateWithoutCascadeSchema() {
        assertThrows(DropNotEmptySchemaException.class,
                () -> new SingleDropSchemaMetaDataValidator().validate(mock(SingleRule.class, RETURNS_DEEP_STUBS), createSQLStatementContext("foo_schema", false), mockDatabase()));
    }
    
    @Test
    void assertValidateWithNotExistedSchema() {
        ShardingSphereDatabase database = mockDatabase();
        when(database.getSchema("not_existed_schema")).thenReturn(null);
        assertThrows(SchemaNotFoundException.class,
                () -> new SingleDropSchemaMetaDataValidator().validate(mock(SingleRule.class, RETURNS_DEEP_STUBS), createSQLStatementContext("not_existed_schema", true), database));
    }
    
    @Test
    void assertValidate() {
        new SingleDropSchemaMetaDataValidator().validate(mock(SingleRule.class, RETURNS_DEEP_STUBS), createSQLStatementContext("foo_schema", true), mockDatabase());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = new ShardingSphereSchema();
        schema.putTable("foo_table", new ShardingSphereTable());
        when(result.getSchemas()).thenReturn(Collections.singletonMap("foo_schema", schema));
        return result;
    }
    
    private SQLStatementContext createSQLStatementContext(final String schemaName, final boolean isCascade) {
        PostgreSQLDropSchemaStatement dropSchemaStatement = mock(PostgreSQLDropSchemaStatement.class, RETURNS_DEEP_STUBS);
        when(dropSchemaStatement.isContainsCascade()).thenReturn(isCascade);
        when(dropSchemaStatement.getSchemaNames()).thenReturn(Collections.singleton(new IdentifierValue(schemaName)));
        return new UnknownSQLStatementContext(dropSchemaStatement);
    }
}
