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

import org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.ImportDataSourceParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.StartCDCClientParameter;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;

import java.util.Collections;

public final class Bootstrap {
    
    /**
     * Main entrance.
     *
     * @param args args
     */
    public static void main(final String[] args) {
        // Pay attention to the time zone, to avoid the problem of incorrect time zone, it is best to ensure that the time zone of the program is consistent with the time zone of the database server
        // and mysql-connector-java 5.x version will ignore serverTimezone jdbc parameter and use the default time zone in the program
        // TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ImportDataSourceParameter importDataSourceParam = new ImportDataSourceParameter("jdbc:opengauss://localhost:5432/cdc_db?stringtype=unspecified", "gaussdb", "Root@123");
        StartCDCClientParameter parameter = new StartCDCClientParameter(importDataSourceParam);
        parameter.setAddress("127.0.0.1");
        parameter.setPort(33071);
        parameter.setUsername("root");
        parameter.setPassword("root");
        parameter.setDatabase("sharding_db");
        parameter.setFull(true);
        parameter.setSchemaTables(Collections.singletonList(SchemaTable.newBuilder().setTable("t_order").build()));
        // support MySQL, PostgreSQL, openGauss
        parameter.setDatabaseType("openGauss");
        CDCClient cdcClient = new CDCClient(parameter);
        cdcClient.start();
    }
}
