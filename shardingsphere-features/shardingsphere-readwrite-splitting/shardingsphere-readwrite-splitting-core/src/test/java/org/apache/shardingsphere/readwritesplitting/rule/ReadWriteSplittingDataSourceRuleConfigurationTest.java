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

package org.apache.shardingsphere.readwritesplitting.rule;

import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;

import org.junit.Before;
import org.junit.Test;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class ReadWriteSplittingDataSourceRuleConfigurationTest {

    private ReadwriteSplittingDataSourceRuleConfiguration readwriteSplittingDataSourceRuleConfig;

    private ReadwriteSplittingDataSourceRuleConfiguration readwriteSplittingDataSourceRuleConfigDynamic;

    @Before
    public void setup() {
        readwriteSplittingDataSourceRuleConfig = new ReadwriteSplittingDataSourceRuleConfiguration("ds", "Static", getProperties("write_ds", "read_ds_0,read_ds_1"), "");
        readwriteSplittingDataSourceRuleConfigDynamic = new ReadwriteSplittingDataSourceRuleConfiguration("ds", "Dynamic", getProperties("write_ds", "read_ds_0,read_ds_1"), "");
    }

    @Test
    public void assertGetAutoAwareDataSourceName() {
        assertNull(readwriteSplittingDataSourceRuleConfigDynamic.getProps().getProperty("auto-aware-data-source-name"));
    }

    @Test
    public void assertGetWriteDataSourceName() {
        assertThat(readwriteSplittingDataSourceRuleConfig.getProps().getProperty("write-data-source-name"), is("write_ds"));
    }

    @Test
    public void assertGetReadDataSourceNames() {
        assertThat(readwriteSplittingDataSourceRuleConfig.getProps().getProperty("read-data-source-names"), is("read_ds_0,read_ds_1"));
    }

    private Properties getProperties(final String writeDataSource, final String readDataSources) {
        Properties result = new Properties();
        result.setProperty("write-data-source-name", writeDataSource);
        result.setProperty("read-data-source-names", readDataSources);
        return result;
    }
}
