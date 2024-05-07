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

package org.apache.shardingsphere.logging.it;

import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.logging.yaml.config.YamlAppenderConfiguration;
import org.apache.shardingsphere.logging.yaml.config.YamlLoggerConfiguration;
import org.apache.shardingsphere.logging.yaml.config.YamlLoggingRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    LoggingRuleConfigurationYamlIT() {
        super("yaml/logging-rule.yaml");
    }
    
    @Override
    protected void assertYamlRootConfiguration(final YamlRootConfiguration actual) {
        assertLoggingRule((YamlLoggingRuleConfiguration) actual.getRules().iterator().next());
    }
    
    private void assertLoggingRule(final YamlLoggingRuleConfiguration actual) {
        assertLoggers(actual.getLoggers());
        assertAppenders(actual.getAppenders());
    }
    
    private void assertLoggers(final Collection<YamlLoggerConfiguration> actual) {
        assertThat(actual.size(), is(1));
        assertThat(new ArrayList<>(actual).get(0).getLoggerName(), is("foo_logger"));
        assertThat(new ArrayList<>(actual).get(0).getLevel(), is("INFO"));
        assertTrue(new ArrayList<>(actual).get(0).getAdditivity());
        assertThat(new ArrayList<>(actual).get(0).getAppenderName(), is("foo_appender"));
        assertThat(new ArrayList<>(actual).get(0).getProps().size(), is(2));
        assertThat(new ArrayList<>(actual).get(0).getProps().getProperty("k0"), is("v0"));
        assertThat(new ArrayList<>(actual).get(0).getProps().getProperty("k1"), is("v1"));
    }
    
    private void assertAppenders(final Collection<YamlAppenderConfiguration> actual) {
        assertThat(actual.size(), is(1));
        assertThat(new ArrayList<>(actual).get(0).getAppenderName(), is("foo_appender"));
        assertThat(new ArrayList<>(actual).get(0).getAppenderClass(), is("foo_appender_class"));
        assertThat(new ArrayList<>(actual).get(0).getPattern(), is("sss"));
        assertThat(new ArrayList<>(actual).get(0).getFile(), is("foo_file"));
    }
}
