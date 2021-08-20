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

package org.apache.shardingsphere.test.integration.junit.container.storage.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.test.integration.env.datasource.DataSourceEnvironmentUtil;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;

/**
 * Oracle container.
 */
public class OracleContainer extends ShardingSphereStorageContainer {

    public OracleContainer(final ParameterizedArray parameterizedArray) {
        super("oracle", "oraclelinux:8", new OracleDatabaseType(), false, parameterizedArray);
    }

    @Override
    protected void configure() {
        withInitSQLMapping("/env/" + getParameterizedArray().getScenario() + "/init-sql/oracle");
    }

    @Override
    @SneakyThrows
    protected void execute() {
    }

    @Override
    protected String getUrl(final String dataSourceName) {
        return DataSourceEnvironmentUtil.getURL("Oracle", getHost(), getPort(), dataSourceName);
    }

    @Override
    protected int getPort() {
        return getMappedPort(1521);
    }

    @Override
    protected String getUsername() {
        return "root";
    }

    @Override
    protected String getPassword() {
        return "";
    }
}
