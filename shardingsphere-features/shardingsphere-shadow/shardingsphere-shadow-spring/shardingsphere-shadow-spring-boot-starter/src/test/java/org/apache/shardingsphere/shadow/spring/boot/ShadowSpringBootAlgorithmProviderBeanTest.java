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

import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ShadowSpringBootAlgorithmProviderBeanTest.class)
@SpringBootApplication
@ActiveProfiles("shadow")
public class ShadowSpringBootAlgorithmProviderBeanTest {
    
    @Resource
    private ApplicationContext applicationContext;
    
    @Test
    public void assertAlgorithmProviderBean() {
        Object algorithmBean = applicationContext.getBean("shadow");
        assertThat(algorithmBean.getClass(), equalTo(YamlShadowRuleConfiguration.class));
        YamlShadowRuleConfiguration actual = YamlShadowRuleConfiguration.class.cast(algorithmBean);
        assertThat(actual.getColumn(), is("shadow"));
        assertThat(actual.getShadowMappings(), is(Collections.singletonMap("ds", "shadow_ds")));
    }
}
