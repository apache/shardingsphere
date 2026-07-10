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
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.explain.DialectExplainOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.function.DialectFunctionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.index.DialectIndexOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.join.DialectJoinOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.keygen.DialectGeneratedKeyOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DefaultSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaSemantics;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.version.DialectProtocolVersionOption;
import org.apache.shardingsphere.database.connector.mysql.metadata.database.option.MySQLDataTypeOption;
import org.apache.shardingsphere.database.connector.mysql.metadata.database.option.MySQLFunctionOption;
import org.apache.shardingsphere.database.connector.mysql.metadata.database.option.MySQLGeneratedKeyOption;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Database meta data of MySQL.
 */
public final class MySQLDatabaseMetaData implements DialectDatabaseMetaData {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?.*");
    
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
    public DialectSchemaOption getSchemaOption() {
        return new DefaultSchemaOption(false, null, DialectSchemaSemantics.DATABASE_AS_SCHEMA, false);
    }
    
    @Override
    public DialectIndexOption getIndexOption() {
        return new DialectIndexOption(false, Integer.MAX_VALUE, true);
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
        return Optional.of(new MySQLGeneratedKeyOption());
    }
    
    @Override
    public DialectProtocolVersionOption getProtocolVersionOption() {
        return new DialectProtocolVersionOption("5.7.22");
    }
    
    @Override
    public DialectExplainOption getExplainOption() {
        return MySQLDatabaseMetaData::isExplainAnalyzeSupported;
    }
    
    private static boolean isExplainAnalyzeSupported(final String databaseVersion) {
        Matcher matcher = VERSION_PATTERN.matcher(Objects.toString(databaseVersion, "").trim());
        if (!matcher.matches()) {
            return false;
        }
        int actualMajorVersion = Integer.parseInt(matcher.group(1));
        int actualMinorVersion = null == matcher.group(2) ? 0 : Integer.parseInt(matcher.group(2));
        int actualPatchVersion = null == matcher.group(3) ? 0 : Integer.parseInt(matcher.group(3));
        if (8 != actualMajorVersion) {
            return actualMajorVersion > 8;
        }
        if (0 != actualMinorVersion) {
            return actualMinorVersion > 0;
        }
        return actualPatchVersion >= 18;
    }
    
    @Override
    public DialectFunctionOption getFunctionOption() {
        return new MySQLFunctionOption();
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
