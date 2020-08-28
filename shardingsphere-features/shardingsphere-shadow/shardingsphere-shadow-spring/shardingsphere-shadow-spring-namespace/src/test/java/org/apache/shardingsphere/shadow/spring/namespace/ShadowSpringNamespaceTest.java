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

package org.apache.shardingsphere.shadow.spring.namespace;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.annotation.Resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "classpath:META-INF/spring/shadow-application-context.xml")
public final class ShadowSpringNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Resource
    private ShadowRuleConfiguration shadowRule;
    
    @Test
    public void assertDataSource() {
        assertThat(shadowRule.getColumn(), is("shadow"));
        assertThat(shadowRule.getShadowMappings(), is(ImmutableMap.of("prod_ds", "shadow_ds")));
    }
}
