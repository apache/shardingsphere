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

package org.apache.shardingsphere.scaling.core.config;

import org.apache.shardingsphere.scaling.core.config.utils.RdbmsConfigurationUtil;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPositionManager;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class RdbmsConfigurationTest {
    
    @Test
    public void assertClone() {
        RdbmsConfiguration origin = new RdbmsConfiguration();
        RdbmsConfiguration clone = RdbmsConfiguration.clone(origin);
        assertTrue(origin.equals(clone));
        origin.setTableName("t1");
        assertFalse(origin.equals(clone));
    }
    
    @Test
    public void assertGetWhereCondition() {
        RdbmsConfiguration rdbmsConfiguration = new RdbmsConfiguration();
        assertThat(RdbmsConfigurationUtil.getWhereCondition(rdbmsConfiguration), is(""));
        rdbmsConfiguration.setPrimaryKey("id");
        rdbmsConfiguration.setPositionManager(new PrimaryKeyPositionManager(new PrimaryKeyPosition(0, 10)));
        assertThat(RdbmsConfigurationUtil.getWhereCondition(rdbmsConfiguration), is("WHERE id BETWEEN 0 AND 10"));
    }
}
