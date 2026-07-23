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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;
import java.util.function.UnaryOperator;

/**
 * Factory methods for identifier case policy set.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IdentifierCasePolicyFactory {
    
    /**
     * Create lower-case policy set.
     *
     * @return lower-case policy set
     */
    public static IdentifierCasePolicySet newLowerCasePolicySet() {
        return new IdentifierCasePolicySet(new IdentifierCasePolicy(LookupMode.EXACT, LookupMode.NORMALIZED, IdentifierCasePolicyFactory::toLowerCase,
                IdentifierCasePolicyFactory::isLowerCase));
    }
    
    /**
     * Create upper-case policy set.
     *
     * @return upper-case policy set
     */
    public static IdentifierCasePolicySet newUpperCasePolicySet() {
        return new IdentifierCasePolicySet(new IdentifierCasePolicy(LookupMode.EXACT, LookupMode.NORMALIZED, IdentifierCasePolicyFactory::toUpperCase,
                IdentifierCasePolicyFactory::isUpperCase));
    }
    
    /**
     * Create case-sensitive policy set.
     *
     * @return case-sensitive policy set
     */
    public static IdentifierCasePolicySet newSensitivePolicySet() {
        return new IdentifierCasePolicySet(new IdentifierCasePolicy(LookupMode.EXACT, LookupMode.EXACT, UnaryOperator.identity(), each -> true));
    }
    
    /**
     * Create case-insensitive policy set.
     *
     * @return case-insensitive policy set
     */
    public static IdentifierCasePolicySet newInsensitivePolicySet() {
        return new IdentifierCasePolicySet(new IdentifierCasePolicy(LookupMode.EXACT, LookupMode.NORMALIZED, IdentifierCasePolicyFactory::toLowerCase, each -> true));
    }
    
    /**
     * Create quoted and unquoted case-insensitive policy set.
     *
     * @return quoted and unquoted case-insensitive policy set
     */
    public static IdentifierCasePolicySet newQuotedInsensitivePolicySet() {
        return new IdentifierCasePolicySet(new IdentifierCasePolicy(LookupMode.NORMALIZED, LookupMode.NORMALIZED, IdentifierCasePolicyFactory::toLowerCase, each -> true));
    }
    
    /**
     * Create MySQL case-insensitive policy set.
     *
     * @return MySQL case-insensitive policy set
     */
    public static IdentifierCasePolicySet newMySQLInsensitivePolicySet() {
        return newQuotedInsensitivePolicySet();
    }
    
    private static String toLowerCase(final String value) {
        return value.toLowerCase(Locale.ENGLISH);
    }
    
    private static String toUpperCase(final String value) {
        return value.toUpperCase(Locale.ENGLISH);
    }
    
    private static boolean isLowerCase(final String value) {
        return value.equals(toLowerCase(value));
    }
    
    private static boolean isUpperCase(final String value) {
        return value.equals(toUpperCase(value));
    }
}
