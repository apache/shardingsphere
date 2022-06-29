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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SingleTableDropSchemaMetadataValidatorTest {

    @Test(expected = ShardingSphereException.class)
    public void assertValidateCascadeNotEmpty() {
        SingleTableDropSchemaMetadataValidator singleTableDropSchemaMetadataValidator = new SingleTableDropSchemaMetadataValidator();
        SingleTableRule singleTableRule = mock(SingleTableRule.class, RETURNS_DEEP_STUBS);
        SQLStatementContext<DropSchemaStatement> sqlStatementContext = createSQLStatementContext("foo_schema", false);
        ShardingSphereDatabase database = createMockDatabase("foo_schema");
        singleTableDropSchemaMetadataValidator.validate(singleTableRule, sqlStatementContext, database);
    }

    @Test(expected = ShardingSphereException.class)
    public void assertValidateSchemaNotMatch() {
        SingleTableDropSchemaMetadataValidator singleTableDropSchemaMetadataValidator = new SingleTableDropSchemaMetadataValidator();
        SingleTableRule singleTableRule = mock(SingleTableRule.class, RETURNS_DEEP_STUBS);
        SQLStatementContext<DropSchemaStatement> sqlStatementContext = createSQLStatementContext("foo_schema1", true);
        ShardingSphereDatabase database = createMockDatabase("foo_schema");
        singleTableDropSchemaMetadataValidator.validate(singleTableRule, sqlStatementContext, database);
    }

    @Test
    public void assertValidate() {
        SingleTableDropSchemaMetadataValidator singleTableDropSchemaMetadataValidator = new SingleTableDropSchemaMetadataValidator();
        SingleTableRule singleTableRule = mock(SingleTableRule.class, RETURNS_DEEP_STUBS);
        SQLStatementContext<DropSchemaStatement> sqlStatementContext = createSQLStatementContext("foo_schema", true);
        ShardingSphereDatabase database = createMockDatabase("foo_schema");
        singleTableDropSchemaMetadataValidator.validate(singleTableRule, sqlStatementContext, database);
    }

    private ShardingSphereDatabase createMockDatabase(final String schemaName) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        Map<String, ShardingSphereSchema> map = new HashMap<>();
        ShardingSphereSchema shardingSphereSchema = new ShardingSphereSchema();
        shardingSphereSchema.put("foo_table", new ShardingSphereTable());
        map.put(schemaName, shardingSphereSchema);
        when(result.getSchemas()).thenReturn(map);
        return result;
    }

    private SQLStatementContext<DropSchemaStatement> createSQLStatementContext(final String schemaName, final boolean isCascade) {
        PostgreSQLDropSchemaStatement dropDatabaseStatement = mock(PostgreSQLDropSchemaStatement.class, RETURNS_DEEP_STUBS);
        when(dropDatabaseStatement.isContainsCascade()).thenReturn(isCascade);
        Collection<IdentifierValue> collection = new ArrayList<>();
        collection.add(new IdentifierValue(schemaName));
        when(dropDatabaseStatement.getSchemaNames()).thenReturn(collection);
        SQLStatementContext<DropSchemaStatement> result = new CommonSQLStatementContext(dropDatabaseStatement);
        return result;
    }
}
