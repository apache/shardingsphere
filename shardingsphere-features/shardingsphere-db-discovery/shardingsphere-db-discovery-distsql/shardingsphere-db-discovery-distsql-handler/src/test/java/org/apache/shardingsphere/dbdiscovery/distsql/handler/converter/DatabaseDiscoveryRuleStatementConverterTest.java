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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.converter;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.AbstractDatabaseDiscoverySegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryConstructionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryDefinitionSegment;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DatabaseDiscoveryRuleStatementConverterTest {
    
    @Test
    public void assertConvert() {
        DatabaseDiscoveryRuleConfiguration ruleConfiguration = DatabaseDiscoveryRuleStatementConverter.convert(createDatabaseDiscoveryRuleSegments());
        assertNotNull(ruleConfiguration);
        assertTrue(ruleConfiguration.getDiscoveryTypes().containsKey("definition_mgr"));
        assertTrue(ruleConfiguration.getDiscoveryHeartbeats().containsKey("definition_heartbeat"));
        Iterator<DatabaseDiscoveryDataSourceRuleConfiguration> iterator = ruleConfiguration.getDataSources().iterator();
        DatabaseDiscoveryDataSourceRuleConfiguration configuration = iterator.next();
        assertThat(configuration.getDataSourceNames(), is(Arrays.asList("resource0", "resource1")));
        assertThat(configuration.getName(), is("definition"));
        assertThat(configuration.getDiscoveryTypeName(), is("definition_mgr"));
        assertThat(configuration.getDiscoveryHeartbeatName(), is("definition_heartbeat"));
        configuration = iterator.next();
        assertThat(configuration.getDataSourceNames(), is(Arrays.asList("resource0", "resource1")));
        assertThat(configuration.getName(), is("construction"));
        assertThat(configuration.getDiscoveryTypeName(), is("type"));
        assertThat(configuration.getDiscoveryHeartbeatName(), is("heartbeat"));
    }
    
    private Collection<AbstractDatabaseDiscoverySegment> createDatabaseDiscoveryRuleSegments() {
        Properties properties = new Properties();
        properties.put("key", "value");
        final Collection<AbstractDatabaseDiscoverySegment> ruleSegments = new LinkedList<>();
        DatabaseDiscoveryDefinitionSegment databaseDiscoveryDefinitionSegment
                = new DatabaseDiscoveryDefinitionSegment("definition", Arrays.asList("resource0", "resource1"), new AlgorithmSegment("mgr", properties), properties);
        DatabaseDiscoveryConstructionSegment databaseDiscoveryConstructionSegment
                = new DatabaseDiscoveryConstructionSegment("construction", Arrays.asList("resource0", "resource1"), "type", "heartbeat");
        ruleSegments.add(databaseDiscoveryConstructionSegment);
        ruleSegments.add(databaseDiscoveryDefinitionSegment);
        return ruleSegments;
    }
}
