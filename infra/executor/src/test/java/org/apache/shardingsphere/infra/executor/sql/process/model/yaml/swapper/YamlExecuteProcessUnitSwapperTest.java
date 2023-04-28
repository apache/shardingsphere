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

package org.apache.shardingsphere.infra.executor.sql.process.model.yaml.swapper;

import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessStatus;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessUnit;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlExecuteProcessUnitSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        ExecuteProcessUnit executeProcessUnit = new ExecuteProcessUnit(new ExecutionUnit("foo_ds", new SQLUnit("SELECT 1", Collections.emptyList())), ExecuteProcessStatus.START);
        YamlExecuteProcessUnit actual = new YamlExecuteProcessUnitSwapper().swapToYamlConfiguration(executeProcessUnit);
        assertNotNull(actual.getUnitID());
        assertThat(actual.getProcessStatus(), is(ExecuteProcessStatus.START));
    }
    
    @Test
    void assertSwapToObject() {
        assertThrows(UnsupportedOperationException.class, () -> new YamlExecuteProcessUnitSwapper().swapToObject(new YamlExecuteProcessUnit()));
    }
}
