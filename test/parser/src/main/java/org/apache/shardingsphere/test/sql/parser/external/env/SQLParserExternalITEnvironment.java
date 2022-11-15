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

package org.apache.shardingsphere.test.sql.parser.external.env;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * SQL parser external IT environment.
 */
public final class SQLParserExternalITEnvironment {
    
    private static final String SQL_PARSER_EXTERNAL_IT_ENABLED_KEY = "sql.parser.external.it.enabled";
    
    private static final String SQL_PARSER_EXTERNAL_IT_REPORT_PATH= "sql.parser.external.it.report.path";
    
    private static final String SQL_PARSER_EXTERNAL_IT_REPORT_TYPE = "sql.parser.external.it.report.type";
    
    @Getter
    private static final SQLParserExternalITEnvironment INSTANCE = new SQLParserExternalITEnvironment();
    
    @Getter
    private final boolean sqlParserITEnabled;
    
    @Getter
    private final String resultPath;
    
    @Getter
    private final String resultProcessorType;
    
    private SQLParserExternalITEnvironment() {
        Properties props = loadProperties();
        sqlParserITEnabled = Boolean.parseBoolean(
                null == System.getProperty(SQL_PARSER_EXTERNAL_IT_ENABLED_KEY) ? props.get(SQL_PARSER_EXTERNAL_IT_ENABLED_KEY).toString() : System.getProperty(SQL_PARSER_EXTERNAL_IT_ENABLED_KEY));
        resultPath = props.getOrDefault(SQL_PARSER_EXTERNAL_IT_REPORT_PATH, "/tmp/").toString();
        resultProcessorType = props.getOrDefault(SQL_PARSER_EXTERNAL_IT_REPORT_TYPE, "LOG").toString();
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
