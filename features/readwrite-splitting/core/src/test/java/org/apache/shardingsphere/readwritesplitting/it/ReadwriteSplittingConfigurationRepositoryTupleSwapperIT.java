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

package org.apache.shardingsphere.readwritesplitting.it;

import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.readwritesplitting.yaml.swapper.ReadwriteSplittingRuleConfigurationRepositoryTupleSwapper;
import org.apache.shardingsphere.test.it.yaml.RepositoryTupleSwapperIT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ReadwriteSplittingConfigurationRepositoryTupleSwapperIT extends RepositoryTupleSwapperIT {
    
    ReadwriteSplittingConfigurationRepositoryTupleSwapperIT() {
        super("yaml/readwrite-splitting-rule.yaml", new ReadwriteSplittingRuleConfigurationRepositoryTupleSwapper(), false);
    }
    
    @Override
    protected void assertRepositoryTuples(final Collection<RepositoryTuple> actualRepositoryTuples) {
        assertThat(actualRepositoryTuples.size(), is(4));
        List<RepositoryTuple> actual = new ArrayList<>(actualRepositoryTuples);
        assertLoadBalancers(actual.subList(0, 2));
        assertDataSourceGroups(actual.subList(2, 4));
    }
    
    private void assertLoadBalancers(final List<RepositoryTuple> actual) {
        assertThat(actual.get(0).getKey(), is("load_balancers/random"));
        assertThat(actual.get(0).getValue(), is("type: RANDOM\n"));
        assertThat(actual.get(1).getKey(), is("load_balancers/roundRobin"));
        assertThat(actual.get(1).getValue(), is("type: ROUND_ROBIN\n"));
    }
    
    private void assertDataSourceGroups(final List<RepositoryTuple> actual) {
        assertThat(actual.get(0).getKey(), is("data_sources/ds_0"));
        assertThat(actual.get(0).getValue(), is("loadBalancerName: roundRobin\nreadDataSourceNames:\n- write_ds_0_read_0\n- write_ds_0_read_1\nwriteDataSourceName: write_ds_0\n"));
        assertThat(actual.get(1).getKey(), is("data_sources/ds_1"));
        assertThat(actual.get(1).getValue(), is("loadBalancerName: random\nreadDataSourceNames:\n- write_ds_1_read_0\n- write_ds_1_read_1\nwriteDataSourceName: write_ds_1\n"));
    }
}
