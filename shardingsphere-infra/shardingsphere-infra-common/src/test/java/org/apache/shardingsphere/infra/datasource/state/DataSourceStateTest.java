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

package org.apache.shardingsphere.infra.datasource.state;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class DataSourceStateTest {

    @Test
    public void testDisabled() {
        assertEquals(DataSourceState.DISABLED, DataSourceState.getDataSourceState("disabled"));
    }

    @Test
    public void testEnabled() {
        assertEquals(DataSourceState.ENABLED, DataSourceState.getDataSourceState("enabled"));
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenStateIsEmpty() {
        assertThrows("Illegal data source state ``",IllegalArgumentException.class,()->{
           DataSourceState.getDataSourceState("");
        });
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenStateIsNull() {
        assertThrows("Illegal data source state ``",IllegalArgumentException.class,()->{
            DataSourceState.getDataSourceState(null);
        });
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenStateIsInvalid() {
        assertThrows("Illegal data source state `invalid`",IllegalArgumentException.class,()->{
            DataSourceState.getDataSourceState("invalid");
        });
    }
}