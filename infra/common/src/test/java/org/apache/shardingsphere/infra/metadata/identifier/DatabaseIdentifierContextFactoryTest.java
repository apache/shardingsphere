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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRule;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseIdentifierContextFactoryTest {
    
    @Test
    void assertCreateDefault() {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.TABLE);
        assertThat(actualRule.getLookupMode(QuoteCharacter.NONE), is(LookupMode.NORMALIZED));
        assertTrue(actualRule.matches("Foo", "foo", QuoteCharacter.NONE));
    }
    
    @Test
    void assertCreateWithProtocolTypeAndProps() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Oracle");
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(databaseType, new ConfigurationProperties(new Properties()));
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.TABLE);
        assertTrue(actualRule.matches("FOO", "foo", QuoteCharacter.NONE));
        assertFalse(actualRule.matches("Foo", "foo", QuoteCharacter.NONE));
    }
    
    @Test
    void assertCreateWithResourceMetaDataAndProps() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        ConfigurationProperties props = new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.METADATA_IDENTIFIER_CASE_SENSITIVITY.getKey(), "sensitive")));
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(databaseType, new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()), props);
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.TABLE);
        assertThat(actualRule.getLookupMode(QuoteCharacter.NONE), is(LookupMode.EXACT));
    }
}
