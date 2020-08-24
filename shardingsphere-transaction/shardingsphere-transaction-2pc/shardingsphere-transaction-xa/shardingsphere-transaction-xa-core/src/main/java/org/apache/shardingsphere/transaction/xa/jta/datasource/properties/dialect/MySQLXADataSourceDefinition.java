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

package org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect;

import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition;

import java.util.Arrays;
import java.util.Collection;

/**
 * XA data source definition for MySQL.
 */
public final class MySQLXADataSourceDefinition implements XADataSourceDefinition {
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
    
    @Override
    public Collection<String> getXADriverClassName() {
        return Arrays.asList("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource", "com.mysql.cj.jdbc.MysqlXADataSource");
    }
}
