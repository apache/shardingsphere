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

package org.apache.shardingsphere.database.connector.mariadb.metadata.database;

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
import org.apache.shardingsphere.database.connector.mysql.metadata.database.MySQLDatabaseMetaData;

import java.sql.Connection;
import java.util.Collections;
import java.util.Optional;

/**
 * Database meta data of MariaDB.
 */
public final class MariaDBDatabaseMetaData implements DialectDatabaseMetaData {
    
    private final DialectDatabaseMetaData delegate = new MySQLDatabaseMetaData();
    
    @Override
    public QuoteCharacter getQuoteCharacter() {
        return delegate.getQuoteCharacter();
    }
    
    @Override
    public IdentifierPatternType getIdentifierPatternType() {
        return delegate.getIdentifierPatternType();
    }
    
    @Override
    public NullsOrderType getDefaultNullsOrderType() {
        return delegate.getDefaultNullsOrderType();
    }
    
    @Override
    public DialectDataTypeOption getDataTypeOption() {
        return delegate.getDataTypeOption();
    }
    
    @Override
    public DialectColumnOption getColumnOption() {
        return delegate.getColumnOption();
    }
    
    @Override
    public DialectConnectionOption getConnectionOption() {
        return delegate.getConnectionOption();
    }
    
    @Override
    public DialectTransactionOption getTransactionOption() {
        return new DialectTransactionOption(false, false, true, false, true, Connection.TRANSACTION_REPEATABLE_READ, false, false, Collections.singleton("org.mariadb.jdbc.MariaDbDataSource"));
    }
    
    @Override
    public DialectJoinOption getJoinOption() {
        return delegate.getJoinOption();
    }
    
    @Override
    public Optional<DialectGeneratedKeyOption> getGeneratedKeyOption() {
        return delegate.getGeneratedKeyOption();
    }
    
    @Override
    public DialectProtocolVersionOption getProtocolVersionOption() {
        return delegate.getProtocolVersionOption();
    }
    
    @Override
    public String getDatabaseType() {
        return "MariaDB";
    }
}
