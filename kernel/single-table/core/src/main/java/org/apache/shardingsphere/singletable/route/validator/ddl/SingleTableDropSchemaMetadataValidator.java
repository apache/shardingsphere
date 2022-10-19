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

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.SchemaNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.singletable.exception.DropNotEmptySchemaException;
import org.apache.shardingsphere.singletable.route.validator.SingleTableMetadataValidator;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.DropSchemaStatementHandler;

/**
 * Single table drop schema metadata validator.
 */
public final class SingleTableDropSchemaMetadataValidator implements SingleTableMetadataValidator<DropSchemaStatement> {
    
    @Override
    public void validate(final SingleTableRule rule, final SQLStatementContext<DropSchemaStatement> sqlStatementContext, final ShardingSphereDatabase database) {
        boolean containsCascade = DropSchemaStatementHandler.containsCascade(sqlStatementContext.getSqlStatement());
        for (IdentifierValue each : sqlStatementContext.getSqlStatement().getSchemaNames()) {
            String schemaName = each.getValue();
            ShardingSphereSchema schema = database.getSchema(schemaName);
            ShardingSpherePreconditions.checkNotNull(schema, () -> new SchemaNotFoundException(schemaName));
            ShardingSpherePreconditions.checkState(containsCascade || schema.getAllTableNames().isEmpty(), () -> new DropNotEmptySchemaException(schemaName));
        }
    }
}
