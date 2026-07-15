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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode
@Getter
final class SQLStatementObjectName {
    
    private final String objectName;
    
    private final String firstIdentifier;
    
    private final QuoteCharacter firstIdentifierQuoteCharacter;
    
    private final boolean qualified;
    
    static SQLStatementObjectName fromNormalizedName(final String objectName) {
        int qualifierSeparatorIndex = objectName.indexOf('.');
        return new SQLStatementObjectName(objectName, -1 == qualifierSeparatorIndex ? objectName : objectName.substring(0, qualifierSeparatorIndex),
                QuoteCharacter.NONE, -1 != qualifierSeparatorIndex);
    }
    
    static SQLStatementObjectName from(final Optional<OwnerSegment> owner, final IdentifierValue identifier) {
        List<IdentifierValue> identifiers = new LinkedList<>();
        owner.ifPresent(optional -> addOwnerIdentifiers(optional, identifiers));
        identifiers.add(identifier);
        return from(identifiers);
    }
    
    static SQLStatementObjectName from(final List<IdentifierValue> identifiers) {
        IdentifierValue firstIdentifier = identifiers.get(0);
        return new SQLStatementObjectName(identifiers.stream().map(IdentifierValue::getValue).collect(Collectors.joining(".")),
                firstIdentifier.getValue(), firstIdentifier.getQuoteCharacter(), 1 < identifiers.size());
    }
    
    private static void addOwnerIdentifiers(final OwnerSegment owner, final Collection<IdentifierValue> result) {
        owner.getOwner().ifPresent(optional -> addOwnerIdentifiers(optional, result));
        result.add(owner.getIdentifier());
    }
}
