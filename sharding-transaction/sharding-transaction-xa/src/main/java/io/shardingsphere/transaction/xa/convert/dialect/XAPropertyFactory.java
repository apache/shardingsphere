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

package io.shardingsphere.transaction.xa.convert.dialect;

import io.shardingsphere.core.rule.DataSourceParameter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Properties;

/**
 * XA property factory.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class XAPropertyFactory {
    
    /**
     * Create XA properties using datasource parameter.
     *
     * @param xaDatabaseType XA database type
     * @param dataSourceParameter datasource parameter
     * @return XA properties
     */
    public static Properties build(final XADatabaseType xaDatabaseType, final DataSourceParameter dataSourceParameter) {
        switch (xaDatabaseType) {
            case MySQL:
                return new MysqlXAProperty(dataSourceParameter).build();
            case PostgreSQL:
                return new PGXAProperty(dataSourceParameter).build();
            case H2:
                return new H2XAProperty(dataSourceParameter).build();
            case SQLServer:
                return new SQLServerXAProperty(dataSourceParameter).build();
            case Oracle:
                return new OracleXAProperty(dataSourceParameter).build();
            default:
                return new Properties();
        }
    }
}
