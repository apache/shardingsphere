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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;

/**
 * Rule of identifier case matching.
 */
public interface IdentifierCaseRule {
    
    /**
     * Get lookup mode for identifier.
     *
     * @param quoteCharacter quote character
     * @return lookup mode
     */
    LookupMode getLookupMode(QuoteCharacter quoteCharacter);
    
    /**
     * Normalize identifier value.
     *
     * @param value identifier value
     * @return normalized identifier value
     */
    String normalize(String value);
    
    /**
     * Judge whether stored identifier matches input identifier.
     *
     * @param storedName stored identifier name
     * @param actualIdentifier input identifier value
     * @param quoteCharacter quote character
     * @return whether matched
     */
    boolean matches(String storedName, String actualIdentifier, QuoteCharacter quoteCharacter);
}
