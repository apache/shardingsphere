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

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;

/**
 * Database identifier context.
 */
public final class DatabaseIdentifierContext {
    
    private volatile IdentifierCasePolicySet policySet;
    
    @Getter
    private volatile boolean heterogeneousTableLookupEnabled;
    
    public DatabaseIdentifierContext(final IdentifierCasePolicySet policySet) {
        this(policySet, false);
    }
    
    public DatabaseIdentifierContext(final IdentifierCasePolicySet policySet, final boolean heterogeneousTableLookupEnabled) {
        this.policySet = policySet;
        this.heterogeneousTableLookupEnabled = heterogeneousTableLookupEnabled;
    }
    
    /**
     * Get identifier case policy for scope.
     *
     * @param identifierScope identifier scope
     * @return identifier case policy
     */
    public IdentifierCasePolicy getPolicy(final IdentifierScope identifierScope) {
        return policySet.getPolicy(identifierScope);
    }
    
    /**
     * Refresh identifier context.
     *
     * @param policySet identifier case policy set
     */
    public synchronized void refresh(final IdentifierCasePolicySet policySet) {
        this.policySet = policySet;
    }
    
    /**
     * Refresh identifier context.
     *
     * @param policySet identifier case policy set
     * @param heterogeneousTableLookupEnabled heterogeneous table lookup enabled or not
     */
    public synchronized void refresh(final IdentifierCasePolicySet policySet, final boolean heterogeneousTableLookupEnabled) {
        this.policySet = policySet;
        this.heterogeneousTableLookupEnabled = heterogeneousTableLookupEnabled;
    }
}
