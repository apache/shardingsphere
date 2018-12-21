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

package io.shardingsphere.transaction.xa.convert.swap;

import io.shardingsphere.core.rule.DataSourceParameter;

import javax.sql.DataSource;
import java.util.Collection;

/**
 * Pick up property from datasource then swap to datasource parameter.
 *
 * @author zhaojun
 */
public interface DataSourceSwapper {
    
    /**
     * Pick up property from datasource then swap to datasource parameter.
     *
     * @param dataSource data source
     * @return dataSource parameter
     */
    DataSourceParameter swap(DataSource dataSource);
    
    /**
     * get data source class names.
     *
     * @return data source class names
     */
    Collection<String> getDataSourceClassNames();
}

