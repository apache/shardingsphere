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

package org.apache.shardingsphere.data.pipeline.cdc.client.parameter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest.SubscriptionMode;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest.TableName;

import java.util.List;

/**
 * Start CDC client parameter.
 */
@Getter
@Setter
@RequiredArgsConstructor
public final class StartCDCClientParameter {
    
    private String databaseType;
    
    private String address;
    
    private int port;
    
    private String username;
    
    private String password;
    
    private String database;
    
    private List<TableName> subscribeTables;
    
    private String subscriptionName;
    
    private SubscriptionMode subscriptionMode = SubscriptionMode.INCREMENTAL;
    
    private boolean incrementalGlobalOrderly;
    
    private final ImportDataSourceParameter importDataSourceParameter;
}
