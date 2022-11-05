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

package org.apache.shardingsphere.test.integration.ha.cases.mysql;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.test.integration.ha.cases.base.BaseITCase;
import org.apache.shardingsphere.test.integration.ha.framework.parameter.HAParameterized;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * MySQL High Availability Integration Test.
 */
@Slf4j
@RunWith(Parameterized.class)
public final class MySQLHaGeneralIT extends BaseITCase {
    
    private final HAParameterized haParameterized;
    
    public MySQLHaGeneralIT(final HAParameterized haParameterized) {
        super(haParameterized);
        this.haParameterized = haParameterized;
    }
    
    @Parameters(name = "{0}")
    public static Collection<HAParameterized> getParameters() {
        Collection<HAParameterized> result = new LinkedList<>();
        MySQLDatabaseType databaseType = new MySQLDatabaseType();
        for (String each : ENV.listStorageContainerImages(databaseType)) {
            result.add(new HAParameterized(databaseType, each, "mysql_ha"));
        }
        return result;
    }
    
    @Test
    public void assertProxyJdbcConnection() {
        List<DataSource> mappedDataSources = getMappedDataSources();
        List<DataSource> exposedDataSources = getExposedDataSources();
        // TODO add the MySQL HA logic here.
        mappedDataSources.forEach(each -> log.info(each.toString()));
        exposedDataSources.forEach(each -> log.info(each.toString()));
    }
}
