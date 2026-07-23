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

package org.apache.shardingsphere.database.connector.core.metadata.identifier;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Policy of identifier case matching.
 */
@RequiredArgsConstructor
public final class IdentifierCasePolicy {
    
    private final LookupMode quotedLookupMode;
    
    private final LookupMode unquotedLookupMode;
    
    private final UnaryOperator<String> normalizer;
    
    private final Predicate<String> unquotedStoredNamePredicate;
    
    /**
     * Get lookup mode for identifier.
     *
     * @param quoteCharacter quote character
     * @return lookup mode
     */
    public LookupMode getLookupMode(final QuoteCharacter quoteCharacter) {
        return QuoteCharacter.NONE == quoteCharacter ? unquotedLookupMode : quotedLookupMode;
    }
    
    /**
     * Normalize identifier value.
     *
     * @param value identifier value
    * @return normalized identifier value
    */
    public String normalize(final String value) {
        return normalizer.apply(value);
    }
    
    /**
     * Judge whether stored identifier matches input identifier.
     *
     * @param storedName stored identifier name
     * @param actualIdentifier input identifier value
     * @param quoteCharacter quote character
    * @return whether matched
    */
    public boolean matches(final String storedName, final String actualIdentifier, final QuoteCharacter quoteCharacter) {
        if (QuoteCharacter.NONE != quoteCharacter) {
            return LookupMode.EXACT == quotedLookupMode
                    ? storedName.equals(actualIdentifier)
                    : normalize(storedName).equals(normalize(actualIdentifier));
        }
        return unquotedStoredNamePredicate.test(storedName) && normalize(storedName).equals(normalize(actualIdentifier));
    }
}
