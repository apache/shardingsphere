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

package org.apache.shardingsphere.data.pipeline.cdc.client.example;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient;
import org.apache.shardingsphere.data.pipeline.cdc.client.config.CDCClientConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.client.handler.LoggerExceptionHandler;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.CDCLoginParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.StartStreamingParameter;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;

import java.util.Collections;

@Slf4j
public final class Bootstrap {
    
    /**
     * Main entrance.
     *
     * @param args args
     */
    @SneakyThrows(InterruptedException.class)
    public static void main(final String[] args) {
        // Pay attention to the time zone, to avoid the problem of incorrect time zone, it is best to ensure that the time zone of the program is consistent with the time zone of the database server
        // TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        String address = "127.0.0.1";
        CDCClientConfiguration clientConfig = new CDCClientConfiguration(address, 33071, records -> log.info("records: {}", records), new LoggerExceptionHandler());
        try (CDCClient cdcClient = new CDCClient(clientConfig)) {
            cdcClient.connect();
            cdcClient.login(new CDCLoginParameter("root", "root"));
            String streamingId = cdcClient.startStreaming(new StartStreamingParameter("sharding_db", Collections.singleton(SchemaTable.newBuilder().setTable("t_order").build()), true));
            log.info("Streaming id={}", streamingId);
            cdcClient.await();
        }
    }
}
