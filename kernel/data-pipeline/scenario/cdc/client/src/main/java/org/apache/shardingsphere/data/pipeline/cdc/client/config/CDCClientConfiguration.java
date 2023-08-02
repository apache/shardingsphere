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

package org.apache.shardingsphere.data.pipeline.cdc.client.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.cdc.client.handler.ExceptionHandler;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;

import java.util.List;
import java.util.function.Consumer;

/**
 * CDC client configuration.
 */
@RequiredArgsConstructor
@Getter
public final class CDCClientConfiguration {
    
    private final String address;
    
    private final int port;
    
    private final Consumer<List<Record>> dataConsumer;
    
    private final ExceptionHandler exceptionHandler;
    
    private final int timeoutMills;
    
    public CDCClientConfiguration(final String address, final int port, final Consumer<List<Record>> dataConsumer, final ExceptionHandler exceptionHandler) {
        this.address = address;
        this.port = port;
        this.dataConsumer = dataConsumer;
        this.exceptionHandler = exceptionHandler;
        this.timeoutMills = 5000;
    }
}
