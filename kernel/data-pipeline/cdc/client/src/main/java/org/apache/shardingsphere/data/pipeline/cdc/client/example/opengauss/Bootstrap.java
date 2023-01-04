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

package org.apache.shardingsphere.data.pipeline.cdc.client.example.opengauss;

import org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.StartCDCClientParameter;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest.SubscriptionMode;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest.TableName;

import java.util.Collections;

public final class Bootstrap {
    
    /**
     * Main entrance.
     *
     * @param args args
     */
    public static void main(final String[] args) {
        StartCDCClientParameter parameter = new StartCDCClientParameter();
        parameter.setAddress("127.0.0.1");
        parameter.setPort(33071);
        parameter.setUsername("root");
        parameter.setPassword("root");
        parameter.setDatabase("sharding_db");
        parameter.setSubscriptionMode(SubscriptionMode.FULL);
        parameter.setSubscriptionName("subscribe_sharding_db");
        parameter.setIncrementalGlobalOrderly(true);
        parameter.setSubscribeTables(Collections.singletonList(TableName.newBuilder().setName("t_order").build()));
        parameter.setDatabaseType("openGauss");
        CDCClient cdcClient = new CDCClient(parameter);
        cdcClient.start();
    }
}
