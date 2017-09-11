/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.json;

import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.NamedDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source map json converter.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceMapJson {
    
    static {
        GsonFactory.registerTypeAdapter(NamedDataSource.class, new DataSourceGsonTypeAdapter());
    }
    
    /**
     * Convert data source map to json.
     * 
     * @param dataSources data source map
     * @return sharding rule configuration json string
     */
    public static String toJson(final Map<String, DataSource> dataSources) {
        Collection<NamedDataSource> result = new LinkedList<>();
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            result.add(new NamedDataSource(entry.getKey(), entry.getValue()));
        }
        return GsonFactory.getGson().toJson(result);
    }
}
