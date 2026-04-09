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
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;

import java.util.Locale;
import java.util.function.UnaryOperator;

/**
 * Factory methods for identifier case rule sets.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IdentifierCaseRuleSets {
    
    /**
     * Create lower-case rule set.
     *
     * @return lower-case rule set
     */
    public static IdentifierCaseRuleSet newLowerCaseRuleSet() {
        return new IdentifierCaseRuleSet(new StandardIdentifierCaseRule(LookupMode.EXACT, LookupMode.NORMALIZED, IdentifierCaseRuleSets::toLowerCase, IdentifierCaseRuleSets::isLowerCase));
    }
    
    /**
     * Create upper-case rule set.
     *
     * @return upper-case rule set
     */
    public static IdentifierCaseRuleSet newUpperCaseRuleSet() {
        return new IdentifierCaseRuleSet(new StandardIdentifierCaseRule(LookupMode.EXACT, LookupMode.NORMALIZED, IdentifierCaseRuleSets::toUpperCase, IdentifierCaseRuleSets::isUpperCase));
    }
    
    /**
     * Create case-sensitive rule set.
     *
     * @return case-sensitive rule set
     */
    public static IdentifierCaseRuleSet newSensitiveRuleSet() {
        return new IdentifierCaseRuleSet(new StandardIdentifierCaseRule(LookupMode.EXACT, LookupMode.EXACT, UnaryOperator.identity(), each -> true));
    }
    
    /**
     * Create case-insensitive rule set.
     *
     * @return case-insensitive rule set
     */
    public static IdentifierCaseRuleSet newInsensitiveRuleSet() {
        return new IdentifierCaseRuleSet(new StandardIdentifierCaseRule(LookupMode.EXACT, LookupMode.NORMALIZED, IdentifierCaseRuleSets::toLowerCase, each -> true));
    }
    
    /**
     * Create MySQL case-insensitive rule set.
     *
     * @return MySQL case-insensitive rule set
     */
    public static IdentifierCaseRuleSet newMySQLInsensitiveRuleSet() {
        return new IdentifierCaseRuleSet(new StandardIdentifierCaseRule(LookupMode.NORMALIZED, LookupMode.NORMALIZED, IdentifierCaseRuleSets::toLowerCase, each -> true));
    }
    
    /**
     * Create dialect default rule set.
     *
     * @param identifierPatternType identifier pattern type
     * @param caseSensitive case-sensitive flag
     * @return dialect default rule set
     */
    public static IdentifierCaseRuleSet newDialectDefaultRuleSet(final IdentifierPatternType identifierPatternType, final boolean caseSensitive) {
        switch (identifierPatternType) {
            case LOWER_CASE:
                return newLowerCaseRuleSet();
            case UPPER_CASE:
                return newUpperCaseRuleSet();
            case KEEP_ORIGIN:
            default:
                return caseSensitive ? newSensitiveRuleSet() : newInsensitiveRuleSet();
        }
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
