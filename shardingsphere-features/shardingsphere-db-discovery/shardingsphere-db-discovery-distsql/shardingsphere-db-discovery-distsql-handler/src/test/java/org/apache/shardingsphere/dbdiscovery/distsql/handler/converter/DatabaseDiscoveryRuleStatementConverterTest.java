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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryRuleSegment;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class DatabaseDiscoveryRuleStatementConverterTest {
    
    @Test
    public void assertConvert() {
        DatabaseDiscoveryRuleConfiguration yamlDatabaseDiscoveryRuleConfiguration = DatabaseDiscoveryRuleStatementConverter.convert(createDatabaseDiscoveryRuleSegments());
        assertNotNull(yamlDatabaseDiscoveryRuleConfiguration);
        assertThat(yamlDatabaseDiscoveryRuleConfiguration.getDataSources().iterator().next().getDataSourceNames(), is(Arrays.asList("resource0", "resource1")));
        assertThat(yamlDatabaseDiscoveryRuleConfiguration.getDataSources().iterator().next().getDiscoveryTypeName(), is("pr_ds_MGR"));
        assertThat(yamlDatabaseDiscoveryRuleConfiguration.getDiscoveryTypes().keySet(), is(Collections.singleton("pr_ds_MGR")));
        assertThat(yamlDatabaseDiscoveryRuleConfiguration.getDiscoveryTypes().get("pr_ds_MGR").getType(), is("MGR"));
        assertThat(yamlDatabaseDiscoveryRuleConfiguration.getDiscoveryTypes().get("pr_ds_MGR").getProps().get("test"), is("value"));
    }
    
    private Collection<DatabaseDiscoveryRuleSegment> createDatabaseDiscoveryRuleSegments() {
        Properties props = new Properties();
        props.setProperty("test", "value");
        return Collections.singleton(new DatabaseDiscoveryRuleSegment("pr_ds", Arrays.asList("resource0", "resource1"), "MGR", props));
    }
}
