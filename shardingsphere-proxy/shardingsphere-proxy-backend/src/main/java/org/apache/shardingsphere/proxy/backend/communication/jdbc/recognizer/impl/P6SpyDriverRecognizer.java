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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.impl;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.JDBCDriverURLRecognizerEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.spi.JDBCDriverComposeURLRecognizer;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.spi.JDBCDriverURLRecognizer;

import java.util.Collection;
import java.util.Collections;

/**
 * P6spy Driver JDBC URL recognizer for, this is a compose recognizer.
 */
public final class P6SpyDriverRecognizer implements JDBCDriverComposeURLRecognizer {
    
    public static final String DRIVER_CLASS_NAME = "com.p6spy.engine.spy.P6SpyDriver";
    
    @Override
    public JDBCDriverURLRecognizer getDriverURLRecognizer(final String url) {
        String realUrl = extractRealUrl(url);
        JDBCDriverURLRecognizer driverURLRecognizer = JDBCDriverURLRecognizerEngine.getJDBCDriverURLRecognizer(realUrl);
        return new JDBCDriverURLRecognizer() {
            
            @Override
            public Collection<String> getURLPrefixes() {
                return driverURLRecognizer.getURLPrefixes();
            }
            
            @Override
            public String getDriverClassName() {
                return DRIVER_CLASS_NAME;
            }
            
            @Override
            public String getDatabaseType() {
                return driverURLRecognizer.getDatabaseType();
            }
        };
    }
    
    /**
     * Parses out the real JDBC connection URL by removing "p6spy:".
     *
     * @param url the connection URL
     * @return the parsed URL
     */
    private String extractRealUrl(final String url) {
        return url.replace("p6spy:", "");
    }
    
    @Override
    public String getDatabaseType() {
        throw new ShardingSphereException("Unsupported getDatabaseType method!");
    }
    
    @Override
    public Collection<String> getURLPrefixes() {
        return Collections.singletonList("jdbc:p6spy:");
    }
    
    @Override
    public String getDriverClassName() {
        return DRIVER_CLASS_NAME;
    }
}
