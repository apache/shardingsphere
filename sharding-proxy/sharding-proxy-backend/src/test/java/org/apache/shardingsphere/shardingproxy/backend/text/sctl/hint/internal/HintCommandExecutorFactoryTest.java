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

package org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal;

import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintAddDatabaseShardingValueExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintAddTableShardingValueExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintClearExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintErrorFormatExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintErrorParameterExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintSetDatabaseShardingValueExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintSetMasterOnlyExecutor;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class HintCommandExecutorFactoryTest {
    
    @Test
    public void assertHintErrorFormatExecutor() {
        String sql = "sctl:hint1 set master_only=true";
        assertThat(HintCommandExecutorFactory.newInstance(sql), instanceOf(HintErrorFormatExecutor.class));
    }
    
    @Test
    public void assertHintSetMasterOnlyExecutor() {
        String sql = "sctl:hint set master_only=false";
        assertThat(HintCommandExecutorFactory.newInstance(sql), instanceOf(HintSetMasterOnlyExecutor.class));
    }
    
    @Test
    public void assertHintSetDatabaseShardingValueExecutor() {
        String sql = "sctl:hint set DatabaseShardingValue=100";
        assertThat(HintCommandExecutorFactory.newInstance(sql), instanceOf(HintSetDatabaseShardingValueExecutor.class));
    }
    
    @Test
    public void assertHintAddDatabaseShardingValueExecutor() {
        String sql = "sctl:hint addDatabaseShardingValue user=100";
        assertThat(HintCommandExecutorFactory.newInstance(sql), instanceOf(HintAddDatabaseShardingValueExecutor.class));
    }
    
    @Test
    public void assertHintAddTableShardingValueExecutor() {
        String sql = "sctl:hint addTableShardingValue user=100";
        assertThat(HintCommandExecutorFactory.newInstance(sql), instanceOf(HintAddTableShardingValueExecutor.class));
    }
    
    @Test
    public void assertHintClearExecutor() {
        String sql = "sctl:hint clear ";
        assertThat(HintCommandExecutorFactory.newInstance(sql), instanceOf(HintClearExecutor.class));
    }
    
    @Test
    public void assertHintErrorParameterExecutor() {
        String sql = "sctl:hint bad parameters ";
        assertThat(HintCommandExecutorFactory.newInstance(sql), instanceOf(HintErrorParameterExecutor.class));
    }
}
