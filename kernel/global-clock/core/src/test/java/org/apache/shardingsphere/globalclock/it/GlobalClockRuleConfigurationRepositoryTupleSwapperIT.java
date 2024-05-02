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

package org.apache.shardingsphere.globalclock.it;

import org.apache.shardingsphere.globalclock.yaml.swapper.GlobalClockRuleConfigurationRepositoryTupleSwapper;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.test.it.yaml.RepositoryTupleSwapperIT;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GlobalClockRuleConfigurationRepositoryTupleSwapperIT extends RepositoryTupleSwapperIT {
    
    GlobalClockRuleConfigurationRepositoryTupleSwapperIT() {
        super("yaml/global-clock-rule.yaml", new GlobalClockRuleConfigurationRepositoryTupleSwapper(), true);
    }
    
    @Override
    protected void assertRepositoryTuples(final Collection<RepositoryTuple> actualRepositoryTuples) {
        assertThat(actualRepositoryTuples.size(), is(1));
        Iterator<RepositoryTuple> iterator = actualRepositoryTuples.iterator();
        assertGlobalClock(iterator.next());
    }
    
    private void assertGlobalClock(final RepositoryTuple actual) {
        assertThat(actual.getKey(), is("global_clock"));
        assertThat(actual.getValue(), is("enabled: true\nprops:\n  k0: v0\n  k1: v1\nprovider: FIXTURE\ntype: FIXTURE\n"));
    }
}
