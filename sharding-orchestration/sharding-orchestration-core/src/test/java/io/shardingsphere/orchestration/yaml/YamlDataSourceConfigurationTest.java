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

package io.shardingsphere.orchestration.yaml;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlDataSourceConfigurationTest {
    
    @Test
    public void assertLoadFromYaml() {
        YamlDataSourceConfiguration expected = new YamlDataSourceConfiguration();
        expected.setDataSourceClassName("com.zaxxer.hikari.HikariDataSource");
        expected.setProperties(new LinkedHashMap<String, Object>(4, 1));
        expected.getProperties().put("driverClassName", "org.h2.Driver");
        expected.getProperties().put("jdbcUrl", "jdbc:h2:mem:test_ds;");
        expected.getProperties().put("username", "root");
        expected.getProperties().put("password", null);
        assertYamlDataSourceConfiguration(loadYamlDataSourceConfiguration("/yaml/datasource-configuration.yaml"), expected);
    }
    
    private YamlDataSourceConfiguration loadYamlDataSourceConfiguration(final String yamlFile) {
        return new Yaml().loadAs(YamlDataSourceConfigurationTest.class.getResourceAsStream(yamlFile), YamlDataSourceConfiguration.class);
    }
    
    private void assertYamlDataSourceConfiguration(final YamlDataSourceConfiguration actual, final YamlDataSourceConfiguration expected) {
        assertThat(actual.getDataSourceClassName(), is(expected.getDataSourceClassName()));
        assertThat(actual.getProperties().size(), is(4));
        assertTrue(actual.getProperties().containsKey("driverClassName"));
        assertThat(actual.getProperties().get("driverClassName"), is(expected.getProperties().get("driverClassName")));
        assertTrue(actual.getProperties().containsKey("jdbcUrl"));
        assertThat(actual.getProperties().get("jdbcUrl"), is(expected.getProperties().get("jdbcUrl")));
        assertTrue(actual.getProperties().containsKey("username"));
        assertThat(actual.getProperties().get("username"), is(expected.getProperties().get("username")));
        assertTrue(actual.getProperties().containsKey("password"));
        assertThat(actual.getProperties().get("password"), is(expected.getProperties().get("password")));
    }
    
    @Test
    public void assertWriteToYaml() {
        YamlDataSourceConfiguration actual = new YamlDataSourceConfiguration();
        actual.setDataSourceClassName("com.zaxxer.hikari.HikariDataSource");
        actual.setProperties(new LinkedHashMap<String, Object>(4, 1));
        actual.getProperties().put("driverClassName", "org.h2.Driver");
        actual.getProperties().put("jdbcUrl", "jdbc:h2:mem:test_ds;");
        actual.getProperties().put("username", "root");
        actual.getProperties().put("password", null);
        assertThat(new Yaml(new DefaultYamlRepresenter()).dump(actual), is(
                "!!io.shardingsphere.orchestration.yaml.YamlDataSourceConfiguration\n"
                        + "dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                        + "properties: {driverClassName: org.h2.Driver, jdbcUrl: 'jdbc:h2:mem:test_ds;', username: root,\n  password: null}\n"));
    }
}
