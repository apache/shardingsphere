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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleProvider;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleProviderContext;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleSet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleSets;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.MetadataIdentifierCaseSensitivity;

import javax.sql.DataSource;

/**
 * Resolver of identifier case rules.
 */
public final class IdentifierCaseRuleResolver {
    
    /**
     * Resolve identifier case rules.
     *
     * @param databaseType database type
     * @param props configuration properties
     * @param dataSource data source
     * @return identifier case rules
     */
    public IdentifierCaseRuleSet resolve(final DatabaseType databaseType, final ConfigurationProperties props, final DataSource dataSource) {
        if (null == databaseType || null == databaseType.getType()) {
            return IdentifierCaseRuleSets.newInsensitiveRuleSet();
        }
        MetadataIdentifierCaseSensitivity configuredCaseSensitivity = props.getValue(ConfigurationPropertyKey.METADATA_IDENTIFIER_CASE_SENSITIVITY);
        if (MetadataIdentifierCaseSensitivity.SENSITIVE == configuredCaseSensitivity) {
            return IdentifierCaseRuleSets.newSensitiveRuleSet();
        }
        if (MetadataIdentifierCaseSensitivity.INSENSITIVE == configuredCaseSensitivity) {
            return IdentifierCaseRuleSets.newInsensitiveRuleSet();
        }
        IdentifierCaseRuleProviderContext context = new IdentifierCaseRuleProviderContext(databaseType, dataSource);
        return DatabaseTypedSPILoader.findService(IdentifierCaseRuleProvider.class, databaseType)
                .flatMap(each -> each.provide(context))
                .orElseGet(IdentifierCaseRuleSets::newInsensitiveRuleSet);
    }
}
