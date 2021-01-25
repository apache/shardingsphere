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

package org.apache.shardingsphere.example.transaction.base.seata.raw.jdbc;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 Requirement before running this test:
 1. create undo_log table in demo_ds_0 and demo_ds_1
 2. startup seata-server-1.0.0 (https://github.com/seata/seata/releases)
     make sure:
     - registry.type = "file"
     - config.ype = "file"
     - service.vgroup_mapping.my_test_tx_group = "default"
 */
@Ignore
public final class SeataATOrderServiceTest {
    
    private SeataATOrderService orderService;
    
    @Before
    public void setUp() throws IOException, SQLException {
        orderService = new SeataATOrderService("/META-INF/sharding-databases-tables.yaml");
        orderService.init();
    }
    
    @After
    public void cleanUp() throws SQLException {
        orderService.cleanup();
    }
    
    @Test
    public void assertInsertSuccess() throws SQLException {
        orderService.insert();
        assertThat(orderService.selectAll(), is(10));
    }
    
    @Test
    public void assertInsertFailed() throws SQLException {
        try {
            orderService.insertFailed();
        // CHECKSTYLE:OFF
        } catch (final Exception ignore) {
        // CHECKSTYLE:ON
        }
        assertThat(orderService.selectAll(), is(0));
    }
}
