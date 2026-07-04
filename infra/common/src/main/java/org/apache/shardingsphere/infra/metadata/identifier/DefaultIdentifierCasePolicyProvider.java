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

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyProvider;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyProviderContext;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;

import java.util.Optional;

/**
 * Default provider of identifier case rules.
 */
public final class DefaultIdentifierCasePolicyProvider implements IdentifierCasePolicyProvider {
    
    @Override
    public Optional<IdentifierCasePolicySet> provide(final IdentifierCasePolicyProviderContext context) {
        DialectDatabaseMetaData dialectDatabaseMetaData = DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, context.getDatabaseType());
        return Optional.of(IdentifierCasePolicyFactory.newDialectDefaultPolicySet(dialectDatabaseMetaData.getIdentifierPatternType(), dialectDatabaseMetaData.isCaseSensitive()));
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
