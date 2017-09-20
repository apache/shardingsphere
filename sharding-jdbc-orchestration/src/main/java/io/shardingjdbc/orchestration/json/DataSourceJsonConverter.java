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

package io.shardingjdbc.orchestration.json;

import io.shardingjdbc.core.jdbc.core.datasource.NamedDataSource;
import com.google.gson.reflect.TypeToken;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source map json converter.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceJsonConverter {
    
    static {
        GsonFactory.registerTypeAdapter(NamedDataSource.class, new DataSourceGsonTypeAdapter());
    }
    
    /**
     * Convert data source map to json.
     * 
     * @param dataSources data source map
     * @return data source map json string
     */
    public static String toJson(final Map<String, DataSource> dataSources) {
        Collection<NamedDataSource> result = new LinkedList<>();
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            result.add(new NamedDataSource(entry.getKey(), entry.getValue()));
        }
        return GsonFactory.getGson().toJson(result);
    }
    
    /**
     * Convert data source map from json.
     *
     * @param json data source map json string
     * @return data source map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, DataSource> fromJson(final String json) {
        Map<String, DataSource> result = new LinkedHashMap<>();
        List<NamedDataSource> namedDataSources = GsonFactory.getGson().fromJson(json, new TypeToken<List<NamedDataSource>>() {
            
        }.getType());
        for (NamedDataSource each : namedDataSources) {
            result.put(each.getName(), each.getDataSource());
        }
        return result;
    }
}
