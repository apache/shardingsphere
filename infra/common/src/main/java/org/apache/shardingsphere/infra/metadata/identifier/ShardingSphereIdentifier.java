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

package org.apache.shardingsphere.infra.metadata.identifier;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

/**
 * ShardingSphere identifier.
 */
public final class ShardingSphereIdentifier {
    
    private final CaseInsensitiveString value;
    
    @Getter
    private final String standardizeValue;
    
    private final boolean caseSensitive;
    
    public ShardingSphereIdentifier(final String value) {
        this.value = new CaseInsensitiveString(value);
        standardizeValue = value;
        caseSensitive = false;
    }
    
    public ShardingSphereIdentifier(final String value, final DatabaseType databaseType) {
        this.value = new CaseInsensitiveString(value);
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
        standardizeValue = standardizeValue(value, dialectDatabaseMetaData, false);
        caseSensitive = dialectDatabaseMetaData.isCaseSensitive();
    }
    
    public ShardingSphereIdentifier(final IdentifierValue identifierValue, final DatabaseType databaseType) {
        value = new CaseInsensitiveString(identifierValue.getValue());
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
        standardizeValue = standardizeValue(identifierValue.getValue(), dialectDatabaseMetaData, QuoteCharacter.NONE != identifierValue.getQuoteCharacter());
        caseSensitive = dialectDatabaseMetaData.isCaseSensitive();
    }
    
    private static String standardizeValue(final String value, final DialectDatabaseMetaData dialectDatabaseMetaData, final boolean quoted) {
        if (null == value) {
            return null;
        }
        if (quoted) {
            return value;
        }
        IdentifierPatternType patternType = dialectDatabaseMetaData.getIdentifierPatternType();
        switch (patternType) {
            case UPPER_CASE:
                return value.toUpperCase();
            case LOWER_CASE:
                return value.toLowerCase();
            case KEEP_ORIGIN:
            default:
                return value;
        }
    }
    
    /**
     * Get identifier value.
     *
     * @return identifier value
     */
    public String getValue() {
        return value.toString();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ShardingSphereIdentifier)) {
            return false;
        }
        ShardingSphereIdentifier other = (ShardingSphereIdentifier) obj;
        if (null == getValue() && null == other.getValue()) {
            return true;
        }
        if (null == standardizeValue || null == other.getStandardizeValue()) {
            return false;
        }
        return caseSensitive ? standardizeValue.equals(other.getStandardizeValue()) : value.equals(other.value);
    }
    
    @Override
    public int hashCode() {
        if (null == standardizeValue) {
            return 0;
        }
        return caseSensitive ? standardizeValue.hashCode() : value.hashCode();
    }
    
    @Override
    public String toString() {
        return getValue();
    }
}
