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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(exclude = "nextIndex")
final class SQLStatementObjectName {
    
    private final String objectName;
    
    private final String firstIdentifier;
    
    private final QuoteCharacter firstIdentifierQuoteCharacter;
    
    private final boolean qualified;
    
    private final int nextIndex;
    
    static SQLStatementObjectName fromNormalizedName(final String objectName) {
        int qualifierSeparatorIndex = objectName.indexOf('.');
        return new SQLStatementObjectName(objectName, -1 == qualifierSeparatorIndex ? objectName : objectName.substring(0, qualifierSeparatorIndex),
                QuoteCharacter.NONE, -1 != qualifierSeparatorIndex, 0);
    }
    
    String objectName() {
        return objectName;
    }
    
    String firstIdentifier() {
        return firstIdentifier;
    }
    
    QuoteCharacter firstIdentifierQuoteCharacter() {
        return firstIdentifierQuoteCharacter;
    }
    
    boolean qualified() {
        return qualified;
    }
    
    int nextIndex() {
        return nextIndex;
    }
}
