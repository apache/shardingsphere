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
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.DatabaseDialectIdentifierHandler;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

/**
 * ShardingSphere identifier.
 */
public final class ShardingSphereIdentifier {
    
    private final boolean isCaseSensitive;
    
    private final CaseInsensitiveString value;
    
    public ShardingSphereIdentifier(final String value) {
        isCaseSensitive = false;
        this.value = new CaseInsensitiveString(value);
    }
    
    public ShardingSphereIdentifier(final String value, final DatabaseType databaseType) {
        isCaseSensitive = DatabaseTypedSPILoader.findService(DatabaseDialectIdentifierHandler.class, databaseType).map(DatabaseDialectIdentifierHandler::isCaseSensitive).orElse(false);
        this.value = new CaseInsensitiveString(value);
    }
    
    public ShardingSphereIdentifier(final IdentifierValue value) {
        isCaseSensitive = QuoteCharacter.NONE != value.getQuoteCharacter();
        this.value = new CaseInsensitiveString(value.getValue());
    }
    
    public ShardingSphereIdentifier(final IdentifierValue value, final DatabaseType databaseType) {
        isCaseSensitive = QuoteCharacter.NONE != value.getQuoteCharacter()
                && DatabaseTypedSPILoader.findService(DatabaseDialectIdentifierHandler.class, databaseType).map(DatabaseDialectIdentifierHandler::isCaseSensitive).orElse(false);
        this.value = new CaseInsensitiveString(value.getValue());
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
        if (null == getValue() && null == ((ShardingSphereIdentifier) obj).getValue()) {
            return true;
        }
        return isCaseSensitive ? String.valueOf(getValue()).equals(((ShardingSphereIdentifier) obj).getValue()) : value.equals(((ShardingSphereIdentifier) obj).value);
    }
    
    @Override
    public int hashCode() {
        return isCaseSensitive ? String.valueOf(getValue()).hashCode() : value.hashCode();
    }
    
    @Override
    public String toString() {
        return getValue();
    }
}
