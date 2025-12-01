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

package org.apache.shardingsphere.infra.config.props.temporary;

import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemporaryConfigurationPropertiesTest {
    
    @Test
    void assertGetValue() {
        TemporaryConfigurationProperties actual = new TemporaryConfigurationProperties(createProperties());
        assertTrue((Boolean) actual.getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_ENABLED));
        assertFalse((Boolean) actual.getValue(TemporaryConfigurationPropertyKey.SYSTEM_SCHEMA_METADATA_ASSEMBLY_ENABLED));
        assertThat(actual.getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON), is("0 0/5 * * * ?"));
    }
    
    private Properties createProperties() {
        return PropertiesBuilder.build(
                new Property(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_ENABLED.getKey(), Boolean.TRUE.toString()),
                new Property(TemporaryConfigurationPropertyKey.SYSTEM_SCHEMA_METADATA_ASSEMBLY_ENABLED.getKey(), Boolean.FALSE.toString()),
                new Property(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON.getKey(), "0 0/5 * * * ?"));
    }
    
    @Test
    void assertGetDefaultValue() {
        TemporaryConfigurationProperties actual = new TemporaryConfigurationProperties(new Properties());
        assertFalse((Boolean) actual.getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_ENABLED));
        assertTrue((Boolean) actual.getValue(TemporaryConfigurationPropertyKey.SYSTEM_SCHEMA_METADATA_ASSEMBLY_ENABLED));
        assertThat(actual.getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON), is("0 0/1 * * * ?"));
    }
}
