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

package org.apache.shardingsphere.readwritesplitting.api.rule;

import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Readwrite-splitting data source rule configuration.
 */
@RequiredArgsConstructor
@Getter
public final class ReadwriteSplittingDataSourceRuleConfiguration {

    private final String name;

    private final String type;

    private final Properties props;

    private final String loadBalancerName;

    private ReadwriteSplittingDataSourceRuleConfiguration readwriteSplittingDataSourceRuleConfig;
    private ReadwriteSplittingDataSourceRuleConfiguration readwriteSplittingDataSourceRuleConfigDynamic;

    @Before
    public void setup(){
        readwriteSplittingDataSourceRuleConfig=new ReadwriteSplittingDataSourceRuleConfiguration("ds", "Static", getProperties("write_ds", "read_ds_0,read_ds_1"),"");
        readwriteSplittingDataSourceRuleConfigDynamic=new ReadwriteSplittingDataSourceRuleConfiguration("ds", "Dynamic", getProperties("write_ds", "read_ds_0,read_ds_1"),"");
    }

    /**
     * Get auto aware data source name.
     *
     * @return auto aware data source name
     */
    public Optional<String> getAutoAwareDataSourceName() {
        return Optional.ofNullable(props.getProperty("auto-aware-data-source-name"));
    }

    @Test
    public void assertGetAutoAwareDataSourceName(){

        ReadwriteSplittingDataSourceRuleConfiguration anotherInstance=new ReadwriteSplittingDataSourceRuleConfiguration("ds","Dynamic",getProperties("write_ds","read_ds_0,read_ds_1"),"");
        String actual=readwriteSplittingDataSourceRuleConfigDynamic.props.getProperty("auto-aware-data-source-name");
        String testing=anotherInstance.props.getProperty("auto-aware-data-source-name");
        assertThat(testing,is(actual));
    }

    /**
     * Get write data source name.
     *
     * @return write data source name
     */
    public Optional<String> getWriteDataSourceName() {
        return Optional.ofNullable(props.getProperty("write-data-source-name"));
    }

    @Test
    public void assertGetWriteDataSourceName(){
        ReadwriteSplittingDataSourceRuleConfiguration anotherInstance=new ReadwriteSplittingDataSourceRuleConfiguration("ds","Static",getProperties("write_ds","read_ds_0,read_ds_1"),"");
        String actual=readwriteSplittingDataSourceRuleConfig.props.getProperty("write-data-source-name");
        String testing=anotherInstance.props.getProperty("write-data-source-name");
        assertThat(testing,is(actual));
    }

    /**
     * Get read data source names.
     *
     * @return read data source names
     */
    public Optional<String> getReadDataSourceNames() {
        return Optional.ofNullable(props.getProperty("read-data-source-names"));
    }

    @Test
    public void assertGetReadDataSourceNames(){
        ReadwriteSplittingDataSourceRuleConfiguration anotherInstance=new ReadwriteSplittingDataSourceRuleConfiguration("ds","Static",getProperties("write_ds","read_ds_0,read_ds_1"),"");
        assertThat(anotherInstance.props.getProperty("read-data-source-names"),is(Arrays.asList("read_ds_0", "read_ds_1")));
    }

    private Properties getProperties(final String writeDataSource, final String readDataSources) {
        Properties result = new Properties();
        result.setProperty("write-data-source-name", writeDataSource);
        result.setProperty("read-data-source-names", readDataSources);
        return result;
    }
}
