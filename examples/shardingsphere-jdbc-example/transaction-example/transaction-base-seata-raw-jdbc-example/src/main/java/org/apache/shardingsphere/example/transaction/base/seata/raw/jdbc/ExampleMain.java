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

/**
 Requirement before running this test:
 1. create undo_log table in demo_ds_0 and demo_ds_1
 2. startup seata-server-1.0.0 (https://github.com/seata/seata/releases)
 make sure:
 - registry.type = "file"
 - config.ype = "file"
 - service.vgroup_mapping.my_test_tx_group = "default"
 */
public final class ExampleMain {
    
    public static void main(final String[] args) throws Exception {
        SeataATOrderService orderService = new SeataATOrderService("/META-INF/sharding-databases-tables.yaml");
        orderService.init();
        orderService.insert();
        orderService.selectAll();
        orderService.cleanup();
    }
}
