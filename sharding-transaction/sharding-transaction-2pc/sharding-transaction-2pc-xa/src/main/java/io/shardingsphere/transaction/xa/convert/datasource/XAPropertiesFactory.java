/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.xa.convert.datasource;

import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.xa.convert.datasource.dialect.H2XAProperties;
import io.shardingsphere.transaction.xa.convert.datasource.dialect.MySQLXAProperties;
import io.shardingsphere.transaction.xa.convert.datasource.dialect.OracleXAProperties;
import io.shardingsphere.transaction.xa.convert.datasource.dialect.PostgreSQLXAProperties;
import io.shardingsphere.transaction.xa.convert.datasource.dialect.SQLServerXAProperties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Properties;

/**
 * XA properties factory.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XAPropertiesFactory {
    
    /**
     * Build properties for XA.
     *
     * @param xaDatabaseType XA database type
     * @param dataSourceParameter data source parameter
     * @return properties for XA
     */
    public static Properties build(final XADatabaseType xaDatabaseType, final DataSourceParameter dataSourceParameter) {
        switch (xaDatabaseType) {
            case H2:
                return new H2XAProperties().build(dataSourceParameter);
            case MySQL:
                return new MySQLXAProperties().build(dataSourceParameter);
            case PostgreSQL:
                return new PostgreSQLXAProperties().build(dataSourceParameter);
            case Oracle:
                return new OracleXAProperties().build(dataSourceParameter);
            case SQLServer:
                return new SQLServerXAProperties().build(dataSourceParameter);
            default:
                return new Properties();
        }
    }
}
