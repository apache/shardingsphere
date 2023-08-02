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

package org.apache.shardingsphere.transaction.xa.jta.datasource.checker.dialect;

import org.apache.shardingsphere.transaction.xa.jta.datasource.checker.DataSourcePrivilegeChecker;

import javax.sql.DataSource;

/**
 * H2 data source privilege checker.
 */
public final class H2DataSourcePrivilegeChecker implements DataSourcePrivilegeChecker {
    
    @Override
    public void checkPrivilege(final DataSource dataSource) {
    }
    
    @Override
    public String getDatabaseType() {
        return "H2";
    }
}
