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

package org.apache.shardingsphere.database.connector.h2.jdbcurl;

import org.apache.shardingsphere.database.connector.core.jdbcurl.judger.DatabaseInstanceJudger;
import org.apache.shardingsphere.database.connector.core.jdbcurl.judger.DialectDatabaseInstanceJudger;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;

/**
 * Database instance judger for H2.
 */
public final class H2DatabaseInstanceJudger implements DialectDatabaseInstanceJudger {
    
    private static final String MODEL_MEM = "mem";
    
    private static final String MODEL_PWD = "~";
    
    private static final String MODEL_FILE = "file:";
    
    @Override
    public boolean isInSameDatabaseInstance(final ConnectionProperties connectionProps1, final ConnectionProperties connectionProps2) {
        return isSameModel(connectionProps1.getQueryProperties().getProperty("model"), connectionProps2.getQueryProperties().getProperty("model"))
                && DatabaseInstanceJudger.isInSameDatabaseInstance(connectionProps1, connectionProps2);
    }
    
    private boolean isSameModel(final String model1, final String model2) {
        if (MODEL_MEM.equalsIgnoreCase(model1)) {
            return model1.equalsIgnoreCase(model2) || MODEL_PWD.equalsIgnoreCase(model2) || MODEL_FILE.equalsIgnoreCase(model2);
        }
        if (MODEL_PWD.equalsIgnoreCase(model1)) {
            return model1.equalsIgnoreCase(model2) || MODEL_MEM.equalsIgnoreCase(model2) || MODEL_FILE.equalsIgnoreCase(model2);
        }
        if (MODEL_FILE.equalsIgnoreCase(model1)) {
            return model1.equalsIgnoreCase(model2) || MODEL_MEM.equalsIgnoreCase(model2) || MODEL_PWD.equalsIgnoreCase(model2);
        }
        return model1.equalsIgnoreCase(model2);
    }
    
    @Override
    public String getDatabaseType() {
        return "H2";
    }
}
