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

package org.apache.shardingsphere.infra.database.h2;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.spi.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.core.url.UnrecognizedDatabaseURLException;

import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data source meta data for H2.
 */
@Getter
public final class H2DataSourceMetaData implements DataSourceMetaData {
    
    private static final int DEFAULT_PORT = -1;
    
    private static final String DEFAULT_HOST_NAME = "";
    
    private static final String DEFAULT_H2_MODEL = "";
    
    private static final String MODEL_MEM = "mem";
    
    private static final String MODEL_PWD = "~";
    
    private static final String MODEL_FILE = "file:";
    
    private static final Pattern URL_PATTERN = Pattern.compile("jdbc:h2:((?<modelMem>mem|~)[:/](?<catalog>[\\w\\-]+)|"
            + "(?<modelSslOrTcp>ssl:|tcp:)(//)?(?<hostname>[\\w\\-.]+)(:(?<port>\\d{1,4})/)?[/~\\w\\-.]+/(?<name>[\\-\\w]*)|"
            + "(?<modelFile>file:)[/~\\w\\-]+/(?<fileName>[\\-\\w]*));?\\S*", Pattern.CASE_INSENSITIVE);
    
    private final String hostname;
    
    private final String model;
    
    private final int port;
    
    private final String catalog;
    
    private final String schema;
    
    public H2DataSourceMetaData(final String url) {
        Matcher matcher = URL_PATTERN.matcher(url);
        if (!matcher.find()) {
            throw new UnrecognizedDatabaseURLException(url, URL_PATTERN.pattern());
        }
        String portFromMatcher = matcher.group("port");
        String catalogFromMatcher = matcher.group("catalog");
        String nameFromMatcher = matcher.group("name");
        String fileNameFromMatcher = matcher.group("fileName");
        String hostnameFromMatcher = matcher.group("hostname");
        boolean setPort = null != portFromMatcher && !portFromMatcher.isEmpty();
        String name = null == nameFromMatcher ? fileNameFromMatcher : nameFromMatcher;
        hostname = null == hostnameFromMatcher ? DEFAULT_HOST_NAME : hostnameFromMatcher;
        port = setPort ? Integer.parseInt(portFromMatcher) : DEFAULT_PORT;
        catalog = null == catalogFromMatcher ? name : catalogFromMatcher;
        schema = null;
        String modelMemFromMatcher = matcher.group("modelMem");
        String modelSslOrTcpFromMatcher = matcher.group("modelSslOrTcp");
        String modelFileFromMatcher = matcher.group("modelFile");
        if (null == modelMemFromMatcher) {
            model = null == modelSslOrTcpFromMatcher ? modelFileFromMatcher : modelSslOrTcpFromMatcher;
        } else {
            model = modelMemFromMatcher;
        }
    }
    
    @Override
    public Properties getQueryProperties() {
        return new Properties();
    }
    
    @Override
    public Properties getDefaultQueryProperties() {
        return new Properties();
    }
    
    @Override
    public boolean isInSameDatabaseInstance(final DataSourceMetaData dataSourceMetaData) {
        if (!(dataSourceMetaData instanceof H2DataSourceMetaData)) {
            return false;
        }
        if (!isSameModel(getModel(), ((H2DataSourceMetaData) dataSourceMetaData).getModel())) {
            return false;
        }
        return DEFAULT_HOST_NAME.equals(hostname) && DEFAULT_PORT == port ? Objects.equals(schema, dataSourceMetaData.getSchema())
                : DataSourceMetaData.super.isInSameDatabaseInstance(dataSourceMetaData);
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
}
