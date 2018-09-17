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

import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.jdbc.orchestration.internal.yaml.representer.DefaultConfigurationRepresenter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * Data source parameter converter.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceParameterConverter {
    
    private static final Yaml YAML = new Yaml(new DefaultConfigurationRepresenter());
    
    /**
     * Convert schema data source parameter map to Yaml string.
     *
     * @param schemaDataSourceMap Schema data source map
     * @return Yaml string
     */
    public static String dataSourceParameterMapToYaml(final Map<String, Map<String, DataSourceParameter>> schemaDataSourceMap) {
        return YAML.dumpAsMap(schemaDataSourceMap);
    }
    
    /**
     * Convert yaml to schema data source parameter map.
     *
     * @param dataSourceParameterMapYamlString String in yaml.
     * @return Schema data source parameter Map.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, DataSourceParameter>> dataSourceParameterMapFromYaml(final String dataSourceParameterMapYamlString) {
        return (Map<String, Map<String, DataSourceParameter>>) YAML.load(dataSourceParameterMapYamlString);
    }
}
