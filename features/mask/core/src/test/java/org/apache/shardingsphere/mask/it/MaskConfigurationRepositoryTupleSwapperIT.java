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

package org.apache.shardingsphere.mask.it;

import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.mask.yaml.swapper.MaskRuleConfigurationRepositoryTupleSwapper;
import org.apache.shardingsphere.test.it.yaml.RepositoryTupleSwapperIT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MaskConfigurationRepositoryTupleSwapperIT extends RepositoryTupleSwapperIT {
    
    MaskConfigurationRepositoryTupleSwapperIT() {
        super("yaml/mask-rule.yaml", new MaskRuleConfigurationRepositoryTupleSwapper(), false);
    }
    
    @Override
    protected void assertRepositoryTuples(final Collection<RepositoryTuple> actualRepositoryTuples) {
        assertThat(actualRepositoryTuples.size(), is(3));
        List<RepositoryTuple> actual = new ArrayList<>(actualRepositoryTuples);
        assertMaskAlgorithms(actual.subList(0, 2));
        assertTable(actual.get(2));
    }
    
    private void assertMaskAlgorithms(final List<RepositoryTuple> actual) {
        assertThat(actual.get(0).getKey(), is("mask_algorithms/keep_first_n_last_m_mask"));
        assertThat(actual.get(0).getValue(), is("props:\n  first-n: 3\n  replace-char: '*'\n  last-m: 4\ntype: KEEP_FIRST_N_LAST_M\n"));
        assertThat(actual.get(1).getKey(), is("mask_algorithms/md5_mask"));
        assertThat(actual.get(1).getValue(), is("type: MD5\n"));
    }
    
    private void assertTable(final RepositoryTuple actual) {
        assertThat(actual.getKey(), is("tables/t_user"));
        assertThat(actual.getValue(), is("columns:\n  telephone:\n    maskAlgorithm: keep_first_n_last_m_mask\n  password:\n    maskAlgorithm: md5_mask\n"));
    }
}
