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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

/**
 * Database identifier context.
 */
@AllArgsConstructor
public final class DatabaseIdentifierContext {
    
    private volatile IdentifierCasePolicySet protocolPolicySet;
    
    private volatile IdentifierCasePolicySet storagePolicySet;
    
    private volatile IdentifierCasePolicySet metaDataPolicySet;
    
    @Getter
    private volatile boolean heterogeneousTableLookupEnabled;
    
    public DatabaseIdentifierContext(final IdentifierCasePolicySet policySet) {
        this(policySet, policySet, policySet, false);
    }
    
    IdentifierCasePolicy getMetaDataPolicy(final IdentifierScope identifierScope) {
        return metaDataPolicySet.getPolicy(identifierScope);
    }
    
    /**
     * Judge whether stored metadata identifier matches input identifier.
     *
     * @param identifierScope identifier scope
     * @param storedName stored metadata identifier name
     * @param identifier input identifier
     * @return whether matched
     */
    public boolean matchesMetaData(final IdentifierScope identifierScope, final String storedName, final IdentifierValue identifier) {
        return metaDataPolicySet.getPolicy(identifierScope).matches(storedName, identifier.getValue(), identifier.getQuoteCharacter());
    }
    
    /**
     * Normalize protocol identifier.
     *
     * @param identifierScope identifier scope
     * @param identifier identifier to be normalized
     * @return normalized protocol identifier
     */
    public String normalizeProtocol(final IdentifierScope identifierScope, final IdentifierValue identifier) {
        return normalize(protocolPolicySet.getPolicy(identifierScope), identifier);
    }
    
    /**
     * Normalize storage identifier.
     *
     * @param identifierScope identifier scope
     * @param identifier identifier to be normalized
     * @return normalized storage identifier
     */
    public String normalizeStorage(final IdentifierScope identifierScope, final IdentifierValue identifier) {
        return normalize(storagePolicySet.getPolicy(identifierScope), identifier);
    }
    
    private String normalize(final IdentifierCasePolicy policy, final IdentifierValue identifier) {
        return QuoteCharacter.NONE == identifier.getQuoteCharacter() && LookupMode.NORMALIZED == policy.getLookupMode(identifier.getQuoteCharacter())
                ? policy.normalize(identifier.getValue())
                : identifier.getValue();
    }
    
    /**
     * Refresh identifier context.
     *
     * @param protocolPolicySet protocol identifier case policy set
     * @param storagePolicySet storage identifier case policy set
     * @param metaDataPolicySet metadata identifier case policy set
     * @param heterogeneousTableLookupEnabled heterogeneous table lookup enabled or not
     */
    public synchronized void refresh(final IdentifierCasePolicySet protocolPolicySet, final IdentifierCasePolicySet storagePolicySet,
                                     final IdentifierCasePolicySet metaDataPolicySet, final boolean heterogeneousTableLookupEnabled) {
        this.protocolPolicySet = protocolPolicySet;
        this.storagePolicySet = storagePolicySet;
        this.metaDataPolicySet = metaDataPolicySet;
        this.heterogeneousTableLookupEnabled = heterogeneousTableLookupEnabled;
    }
}
