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

package org.apache.shardingsphere.shadow.it;

import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.shadow.yaml.swapper.ShadowRuleConfigurationRepositoryTupleSwapper;
import org.apache.shardingsphere.test.it.yaml.RepositoryTupleSwapperIT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShadowRuleConfigurationRepositoryTupleSwapperIT extends RepositoryTupleSwapperIT {
    
    ShadowRuleConfigurationRepositoryTupleSwapperIT() {
        super("yaml/shadow-rule.yaml", new ShadowRuleConfigurationRepositoryTupleSwapper(), false);
    }
    
    @Override
    protected void assertRepositoryTuples(final Collection<RepositoryTuple> actualRepositoryTuples) {
        assertThat(actualRepositoryTuples.size(), is(9));
        List<RepositoryTuple> actual = new ArrayList<>(actualRepositoryTuples);
        assertAlgorithms(actual.subList(0, 4));
        assertDefaultAlgorithm(actual.get(4));
        assertDataSources(actual.get(5));
        assertTables(actual.subList(6, 9));
    }
    
    private void assertAlgorithms(final List<RepositoryTuple> actual) {
        assertThat(actual.get(0).getKey(), is("algorithms/user-id-insert-match-algorithm"));
        assertThat(actual.get(0).getValue(), is("props:\n  regex: '[1]'\n  column: user_id\n  operation: insert\ntype: REGEX_MATCH\n"));
        assertThat(actual.get(1).getKey(), is("algorithms/user-id-update-match-algorithm"));
        assertThat(actual.get(1).getValue(), is("props:\n  regex: '[1]'\n  column: user_id\n  operation: update\ntype: REGEX_MATCH\n"));
        assertThat(actual.get(2).getKey(), is("algorithms/user-id-select-match-algorithm"));
        assertThat(actual.get(2).getValue(), is("props:\n  regex: '[1]'\n  column: user_id\n  operation: select\ntype: REGEX_MATCH\n"));
        assertThat(actual.get(3).getKey(), is("algorithms/sql-hint-algorithm"));
        assertThat(actual.get(3).getValue(), is("props:\n  shadow: true\n  foo: bar\ntype: SQL_HINT\n"));
    }
    
    private void assertDefaultAlgorithm(final RepositoryTuple actual) {
        assertThat(actual.getKey(), is("default_algorithm_name"));
        assertThat(actual.getValue(), is("sql-hint-algorithm"));
    }
    
    private void assertDataSources(final RepositoryTuple actual) {
        assertThat(actual.getKey(), is("data_sources/shadowDataSource"));
        assertThat(actual.getValue(), is("productionDataSourceName: ds\nshadowDataSourceName: ds_shadow\n"));
    }
    
    private void assertTables(final List<RepositoryTuple> actual) {
        assertThat(actual.get(0).getKey(), is("tables/t_order"));
        assertThat(actual.get(0).getValue(), is("dataSourceNames:\n- shadowDataSource\nshadowAlgorithmNames:\n- user-id-insert-match-algorithm\n- user-id-select-match-algorithm\n"));
        assertThat(actual.get(1).getKey(), is("tables/t_order_item"));
        assertThat(actual.get(1).getValue(), is("dataSourceNames:\n- shadowDataSource\n"
                + "shadowAlgorithmNames:\n- user-id-insert-match-algorithm\n- user-id-update-match-algorithm\n- user-id-select-match-algorithm\n"));
        assertThat(actual.get(2).getKey(), is("tables/t_address"));
        assertThat(actual.get(2).getValue(), is("dataSourceNames:\n- shadowDataSource\n"
                + "shadowAlgorithmNames:\n- user-id-insert-match-algorithm\n- user-id-select-match-algorithm\n- sql-hint-algorithm\n"));
    }
}
