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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.beans.Transient;
import java.util.Optional;
import java.util.Properties;

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

    private ReadwriteSplittingDataSourceRule readwriteSplittingDataSourceRule;

    @Before
    public void setup(){
        readwriteSplittingDataSourceRule=new ReadwriteSplittingDataSourceRule(new ReadwriteSplittingDataSourceRuleConfiguration("test_pr", "Static", getProperties("write_ds", "read_ds_0,read_ds_1"), ""), new RandomReplicaLoadBalanceAlgorithm());
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
        ReadwriteSplittingAutoDataSourceRule actual=readwriteSplittingDataSourceRule.getAutoAwareDataSourceName();
        assertThat(actual,is(name));
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

    }
}
