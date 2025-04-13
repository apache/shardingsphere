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

package org.apache.shardingsphere.infra.database.postgresql.metadata.database;

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.core.metadata.database.option.DialectDatabaseTransactionOption;

import java.sql.Types;
import java.util.Map;
import java.util.Optional;

/**
 * Database meta data of PostgreSQL.
 */
public final class PostgreSQLDatabaseMetaData implements DialectDatabaseMetaData {
    
    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.QUOTE;
    }
    
    @Override
    public Map<String, Integer> getExtraDataTypes() {
        Map<String, Integer> result = new CaseInsensitiveMap<>();
        result.put("SMALLINT", Types.SMALLINT);
        result.put("INT", Types.INTEGER);
        result.put("INTEGER", Types.INTEGER);
        result.put("BIGINT", Types.BIGINT);
        result.put("DECIMAL", Types.DECIMAL);
        result.put("NUMERIC", Types.NUMERIC);
        result.put("REAL", Types.REAL);
        result.put("BOOL", Types.BOOLEAN);
        result.put("CHARACTER VARYING", Types.VARCHAR);
        return result;
    }
    
    @Override
    public Optional<Class<?>> findExtraSQLTypeClass(final int dataType, final boolean unsigned) {
        if (Types.SMALLINT == dataType) {
            return Optional.of(Integer.class);
        }
        return Optional.empty();
    }
    
    @Override
    public NullsOrderType getDefaultNullsOrderType() {
        return NullsOrderType.HIGH;
    }
    
    @Override
    public boolean isSchemaAvailable() {
        return true;
    }
    
    @Override
    public Optional<String> getDefaultSchema() {
        return Optional.of("public");
    }
    
    @Override
    public String formatTableNamePattern(final String tableNamePattern) {
        return tableNamePattern.toLowerCase();
    }
    
    @Override
    public DialectDatabaseTransactionOption getTransactionOption() {
        return new DialectDatabaseTransactionOption(false, false, false, true, false);
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
