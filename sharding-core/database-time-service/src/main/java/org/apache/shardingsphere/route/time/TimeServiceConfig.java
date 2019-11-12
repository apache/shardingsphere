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

package org.apache.shardingsphere.route.time;

import lombok.Getter;
import org.apache.shardingsphere.route.time.exception.TimeServiceInitException;

import javax.sql.DataSource;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Time service config.
 * Need to create a time-service.properties under the classpath.
 *
 * @author chenchuangliu
 */
public final class TimeServiceConfig {

    private static final TimeServiceConfig CONFIG = new TimeServiceConfig();

    @Getter
    private String driverClassName;

    @Getter
    private DataSource dataSource;

    private TimeServiceConfig() {
        init();
    }

    private void init() {
        try (InputStream inputStream = TimeServiceConfig.class.getResourceAsStream("/time-service.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            String dataSourceType = (String) properties.remove("dataSourceType");
            this.driverClassName = (String) properties.get("driverClassName");
            Class dataSourceClass = Class.forName(dataSourceType);
            this.dataSource = (DataSource) dataSourceClass.newInstance();
            for (String each : properties.stringPropertyNames()) {
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(each, dataSourceClass);
                Method writeMethod = propertyDescriptor.getWriteMethod();
                writeMethod.invoke(dataSource, properties.getProperty(each));
            }
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | IntrospectionException | InvocationTargetException | IOException e) {
            throw new TimeServiceInitException("please check your time-service.properties", e);
        }
    }

    /**
     * Get TimeServiceConfig instance.
     * @return TimeServiceConfig
     */
    public static TimeServiceConfig getInstance() {
        return CONFIG;
    }
}
