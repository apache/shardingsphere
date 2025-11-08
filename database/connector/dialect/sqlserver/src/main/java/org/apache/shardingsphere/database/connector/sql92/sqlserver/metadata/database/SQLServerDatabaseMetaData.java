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

package org.apache.shardingsphere.database.connector.sql92.sqlserver.metadata.database;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.altertable.DialectAlterTableOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.pagination.DialectPaginationOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DefaultSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;

import java.sql.Connection;
import java.util.Collections;
import java.util.Optional;

/**
 * Database meta data of SQLServer.
 */
public final class SQLServerDatabaseMetaData implements DialectDatabaseMetaData {
    
    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.BRACKETS;
    }
    
    @Override
    public IdentifierPatternType getIdentifierPatternType() {
        return IdentifierPatternType.KEEP_ORIGIN;
    }
    
    @Override
    public NullsOrderType getDefaultNullsOrderType() {
        return NullsOrderType.LOW;
    }
    
    @Override
    public DialectSchemaOption getSchemaOption() {
        return new DefaultSchemaOption(false, "dbo");
    }
    
    @Override
    public DialectTransactionOption getTransactionOption() {
        return new DialectTransactionOption(false, false, false, false, true, Connection.TRANSACTION_READ_COMMITTED, false, false,
                Collections.singleton("com.microsoft.sqlserver.jdbc.SQLServerXADataSource"));
    }
    
    @Override
    public DialectPaginationOption getPaginationOption() {
        return new DialectPaginationOption(true, "ROW_NUMBER", true);
    }
    
    @Override
    public Optional<DialectAlterTableOption> getAlterTableOption() {
        return Optional.of(new DialectAlterTableOption(true, false));
    }
    
    @Override
    public String getDatabaseType() {
        return "SQLServer";
    }
}
