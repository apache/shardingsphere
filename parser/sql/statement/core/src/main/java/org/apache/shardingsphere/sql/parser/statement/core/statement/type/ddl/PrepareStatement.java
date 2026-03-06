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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl;

import lombok.Builder;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

import java.util.Optional;

/**
 * Prepare statement.
 */
@Getter
public final class PrepareStatement extends DDLStatement {
    
    private final SelectStatement select;
    
    private final InsertStatement insert;
    
    private final UpdateStatement update;
    
    private final DeleteStatement delete;
    
    private final SQLStatementAttributes attributes;
    
    @Builder
    private PrepareStatement(final DatabaseType databaseType, final SelectStatement select, final InsertStatement insert, final UpdateStatement update, final DeleteStatement delete) {
        super(databaseType);
        this.select = select;
        this.insert = insert;
        this.update = update;
        this.delete = delete;
        attributes = createAttributes(select, insert, update, delete);
    }
    
    private SQLStatementAttributes createAttributes(final SelectStatement select, final InsertStatement insert, final UpdateStatement update, final DeleteStatement delete) {
        TableExtractor tableExtractor = new TableExtractor();
        Optional.ofNullable(select).ifPresent(tableExtractor::extractTablesFromSelect);
        Optional.ofNullable(insert).ifPresent(tableExtractor::extractTablesFromInsert);
        Optional.ofNullable(update).ifPresent(tableExtractor::extractTablesFromUpdate);
        Optional.ofNullable(delete).ifPresent(tableExtractor::extractTablesFromDelete);
        return new SQLStatementAttributes(new TableSQLStatementAttribute(tableExtractor.getRewriteTables()));
    }
    
    /**
     * Get select statement.
     *
     * @return select statement
     */
    public Optional<SelectStatement> getSelect() {
        return Optional.ofNullable(select);
    }
    
    /**
     * Get insert statement.
     *
     * @return insert statement
     */
    public Optional<InsertStatement> getInsert() {
        return Optional.ofNullable(insert);
    }
    
    /**
     * Get update statement.
     *
     * @return update statement
     */
    public Optional<UpdateStatement> getUpdate() {
        return Optional.ofNullable(update);
    }
    
    /**
     * Get delete statement.
     *
     * @return delete statement
     */
    public Optional<DeleteStatement> getDelete() {
        return Optional.ofNullable(delete);
    }
}
