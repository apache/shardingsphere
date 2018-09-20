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

package io.shardingsphere.jdbc.orchestration.internal.yaml.converter;

import io.shardingsphere.jdbc.orchestration.internal.yaml.representer.DataSourceRepresenter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Data source converter.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceConverter {
    
    private static final Yaml YAML = new Yaml();
    
    /**
     * Convert data source map to Yaml string.
     *
     * @param dataSourceMap Data source map
     * @return Yaml string
     */
    public static String dataSourceMapToYaml(final Map<String, DataSource> dataSourceMap) {
        Yaml yaml = new Yaml(new DataSourceRepresenter(dataSourceMap.entrySet().iterator().next().getValue().getClass()));
        return yaml.dumpAsMap(dataSourceMap);
    }
    
    /**
     * Convert yaml to data source map.
     *
     * @param dataSourceMapYamlString String in yaml.
     * @return Data source Map.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, DataSource> dataSourceMapFromYaml(final String dataSourceMapYamlString) {
        return (Map<String, DataSource>) YAML.load(dataSourceMapYamlString);
    
    }
}
