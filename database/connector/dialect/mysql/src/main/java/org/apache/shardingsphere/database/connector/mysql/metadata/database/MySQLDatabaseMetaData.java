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

package org.apache.shardingsphere.database.connector.mysql.metadata.database;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.column.DialectColumnOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.connection.DialectConnectionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.join.DialectJoinOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.keygen.DialectGeneratedKeyOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.version.DialectProtocolVersionOption;
import org.apache.shardingsphere.database.connector.mysql.metadata.database.option.MySQLDataTypeOption;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Optional;

/**
 * Database meta data of MySQL.
 */
public final class MySQLDatabaseMetaData implements DialectDatabaseMetaData {
    
    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.BACK_QUOTE;
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
    public DialectDataTypeOption getDataTypeOption() {
        return new MySQLDataTypeOption();
    }
    
    @Override
    public DialectColumnOption getColumnOption() {
        return new DialectColumnOption(false);
    }
    
    @Override
    public DialectConnectionOption getConnectionOption() {
        return new DialectConnectionOption(true, true);
    }
    
    @Override
    public DialectTransactionOption getTransactionOption() {
        return new DialectTransactionOption(false, false, true, false, true, Connection.TRANSACTION_REPEATABLE_READ, false, false,
                Arrays.asList("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource", "com.mysql.cj.jdbc.MysqlXADataSource"));
    }
    
    @Override
    public DialectJoinOption getJoinOption() {
        return new DialectJoinOption(true, true);
    }
    
    @Override
    public Optional<DialectGeneratedKeyOption> getGeneratedKeyOption() {
        return Optional.of(new DialectGeneratedKeyOption("GENERATED_KEY"));
    }
    
    @Override
    public DialectProtocolVersionOption getProtocolVersionOption() {
        return new DialectProtocolVersionOption("5.7.22");
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
