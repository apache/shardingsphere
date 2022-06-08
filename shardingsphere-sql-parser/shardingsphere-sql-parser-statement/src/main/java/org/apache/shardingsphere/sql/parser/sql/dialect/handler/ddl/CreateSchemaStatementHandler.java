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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.OpenGaussStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateSchemaStatement;

import java.util.Optional;

/**
 * Create schema statement handler for different dialect SQL statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateSchemaStatementHandler implements SQLStatementHandler {
    
    /**
     * Get username.
     *
     * @param createSchemaStatement create schema statement
     * @return username
     */
    public static Optional<IdentifierValue> getUsername(final CreateSchemaStatement createSchemaStatement) {
        if (createSchemaStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLCreateSchemaStatement) createSchemaStatement).getUsername();
        }
        if (createSchemaStatement instanceof OpenGaussStatement) {
            return ((OpenGaussCreateSchemaStatement) createSchemaStatement).getUsername();
        }
        return Optional.empty();
    }
}
