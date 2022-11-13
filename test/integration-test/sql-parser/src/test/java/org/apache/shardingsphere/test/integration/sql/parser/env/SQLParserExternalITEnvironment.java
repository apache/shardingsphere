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

package org.apache.shardingsphere.test.integration.sql.parser.env;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * SQL parser external IT environment.
 */
@Getter
public final class SQLParserExternalITEnvironment {
    
    private static final SQLParserExternalITEnvironment INSTANCE = new SQLParserExternalITEnvironment();
    
    private final boolean sqlParserITEnabled;
    
    private final String resultPath;
    
    private final String resultProcessorType;
    
    private SQLParserExternalITEnvironment() {
        Properties props = loadProperties();
        sqlParserITEnabled = Boolean.parseBoolean(
                null == System.getProperty("sql.parser.external.it.enabled") ? props.get("sql.parser.external.it.enabled").toString() : System.getProperty("sql.parser.external.it.enabled"));
        resultPath = props.getOrDefault("sql.parser.external.it.report.path", "/tmp/").toString();
        resultProcessorType = props.getOrDefault("sql.parser.external.it.report.type", "LOG").toString();
    }
    
    /**
     * Get instance.
     *
     * @return got instance
     */
    public static SQLParserExternalITEnvironment getInstance() {
        return INSTANCE;
    }
    
    @SneakyThrows(IOException.class)
    private Properties loadProperties() {
        Properties result = new Properties();
        try (InputStream inputStream = SQLParserExternalITEnvironment.class.getClassLoader().getResourceAsStream("env/sql-parser-external-it-env.properties")) {
            result.load(inputStream);
        }
        for (String each : System.getProperties().stringPropertyNames()) {
            result.setProperty(each, System.getProperty(each));
        }
        return result;
    }
}
