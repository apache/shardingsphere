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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.spi.JDBCDriverComposeURLRecognizer;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.spi.JDBCDriverURLRecognizer;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

/**
 * JDBC driver URL recognizer engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCDriverURLRecognizerEngine {
    
    static {
        ShardingSphereServiceLoader.register(JDBCDriverURLRecognizer.class);
    }
    
    /**
     * Get JDBC driver URL recognizer.
     * 
     * @param url JDBC URL
     * @return JDBC driver URL recognizer
     */
    public static JDBCDriverURLRecognizer getJDBCDriverURLRecognizer(final String url) {
        JDBCDriverURLRecognizer result = ShardingSphereServiceLoader.getSingletonServiceInstances(JDBCDriverURLRecognizer.class).stream().filter(each -> isMatchURL(url, each)).findAny()
                .orElseThrow(() -> new ShardingSphereException("Cannot resolve JDBC url `%s`. Please implements `%s` and add to SPI.", url, JDBCDriverURLRecognizer.class.getName()));
        if (result instanceof JDBCDriverComposeURLRecognizer) {
            return ((JDBCDriverComposeURLRecognizer) result).getDriverURLRecognizer(url);
        }
        return result;
    }
    
    private static boolean isMatchURL(final String url, final JDBCDriverURLRecognizer recognizer) {
        return recognizer.getURLPrefixes().stream().anyMatch(url::startsWith);
    }
}
