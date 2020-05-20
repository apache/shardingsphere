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

package org.apache.shardingsphere.spring;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/shadowNamespace.xml")
public class ShadowNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Test
    public void assertDataSource() {
        ShadowRule shadowRule = getShadowRule();
        assertThat(shadowRule.getColumn(), is("shadow"));
        assertThat(shadowRule.getShadowMappings().size(), is(1));
        assertThat(shadowRule.getShadowMappings().get("dbtbl_0"), is("dbtbl_1"));
        assertTrue(getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
    }
    
    private ShadowRule getShadowRule() {
        ShardingSphereDataSource dataSource = applicationContext.getBean("shadowDataSource", ShardingSphereDataSource.class);
        return (ShadowRule) dataSource.getSchemaContexts().getDefaultSchemaContext().getSchema().getRules().iterator().next();
    }
    
    private ConfigurationProperties getProperties() {
        ShardingSphereDataSource dataSource = applicationContext.getBean("shadowDataSource", ShardingSphereDataSource.class);
        return dataSource.getSchemaContexts().getProperties();
    }
}
