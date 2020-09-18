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

package org.apache.shardingsphere.shadow.spring.boot;

import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ShadowSpringBootStarterTest.class)
@SpringBootApplication
@ActiveProfiles("shadow")
public class ShadowSpringBootStarterTest {
    
    @Resource
    private ShadowRuleConfiguration shadowRuleConfiguration;
    
    @Test
    public void assertShadowRuleConfiguration() {
        assertThat(shadowRuleConfiguration.getColumn(), is("shadow"));
        assertThat(shadowRuleConfiguration.getSourceDataSourceNames(), is(Arrays.asList("ds", "ds1")));
        assertThat(shadowRuleConfiguration.getShadowDataSourceNames(), is(Arrays.asList("shadow_ds", "shadow_ds1")));
    }
}
