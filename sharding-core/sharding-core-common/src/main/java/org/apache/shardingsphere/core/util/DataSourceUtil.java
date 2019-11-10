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

package org.apache.shardingsphere.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.sql.DataSource;

import org.apache.shardingsphere.core.exception.UnrecognizeDatasourceInfoException;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceInfo;

public class DataSourceUtil {
    
    /**
     * get DataSourceInfo.
     * 
     * @param dataSource is user configuration
     * @return DataSourceInfo
     */
    public static DataSourceInfo getDataSourceInfo(final DataSource dataSource) {
        try {
            DataSourceInfo sourceInfo = new DataSourceInfo();
            String username = null;
            Field[] fields = dataSource.getClass().getSuperclass().getDeclaredFields();
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    if (field.getName().toLowerCase().contains("url")) {
                        field.setAccessible(true);
                        String jdbcurl = field.get(dataSource) == null ? null : field.get(dataSource).toString();
                        sourceInfo.setUrl(jdbcurl);
                    }
                    if (field.getName().toLowerCase().contains("username")) {
                        field.setAccessible(true);
                        username = field.get(dataSource) == null ? null : field.get(dataSource).toString();
                        sourceInfo.setUsername(username);
                    }
                }
            }

            return sourceInfo;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new UnrecognizeDatasourceInfoException(e);
        }
    }
}
