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

package org.apache.shardingsphere.route.time.mysql;

import org.apache.shardingsphere.core.route.spi.TimeService;
import org.apache.shardingsphere.route.time.exception.TimeServiceInitException;

import javax.sql.DataSource;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

/**
 * MySQL time service.
 * Need to create a mysql-time-service.properties file under the classpath.
 *
 * @author chenchuangliu
 */
public final class MySQLTimeService implements TimeService {

    private static DataSource dataSource;

    static {
        init();
    }

    private static void init() {
        try {
            Properties properties = new Properties();
            properties.load(MySQLTimeService.class.getResourceAsStream("/mysql-time-service.properties"));
            if (properties.isEmpty()) {
                return;
            }
            String dataSourceType = (String) properties.remove("dataSourceType");
            Class dataSourceClass = Class.forName(dataSourceType);
            DataSource dataSource = (DataSource) dataSourceClass.newInstance();
            for (String each : properties.stringPropertyNames()) {
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(each, dataSourceClass);
                Method writeMethod = propertyDescriptor.getWriteMethod();
                writeMethod.invoke(dataSource, properties.getProperty(each));
            }
            MySQLTimeService.dataSource = dataSource;
        } catch (final NullPointerException | IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | IntrospectionException e) {
            throw new TimeServiceInitException("please check your mysql-time-service.properties", e);
        }
    }

    @Override
    public Date getTime() {
        if (null != dataSource) {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("select now()");
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return (Date) resultSet.getObject(1);
            } catch (final SQLException ignore) {
            }
        }
        return new Date();
    }
}
