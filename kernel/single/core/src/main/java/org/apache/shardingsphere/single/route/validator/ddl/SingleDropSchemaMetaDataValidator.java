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
import org.apache.shardingsphere.infra.exception.SchemaNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.single.exception.DropNotEmptySchemaException;
import org.apache.shardingsphere.single.route.validator.SingleMetaDataValidator;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.DropSchemaStatementHandler;

/**
 * Single drop schema meta data validator.
 */
public final class SingleDropSchemaMetaDataValidator implements SingleMetaDataValidator {
    
    @Override
    public void validate(final SingleRule rule, final SQLStatementContext sqlStatementContext, final ShardingSphereDatabase database) {
        DropSchemaStatement dropSchemaStatement = (DropSchemaStatement) sqlStatementContext.getSqlStatement();
        boolean containsCascade = DropSchemaStatementHandler.containsCascade(dropSchemaStatement);
        for (IdentifierValue each : dropSchemaStatement.getSchemaNames()) {
            String schemaName = each.getValue();
            ShardingSphereSchema schema = database.getSchema(schemaName);
            ShardingSpherePreconditions.checkNotNull(schema, () -> new SchemaNotFoundException(schemaName));
            ShardingSpherePreconditions.checkState(containsCascade || schema.getAllTableNames().isEmpty(), () -> new DropNotEmptySchemaException(schemaName));
        }
    }
}
