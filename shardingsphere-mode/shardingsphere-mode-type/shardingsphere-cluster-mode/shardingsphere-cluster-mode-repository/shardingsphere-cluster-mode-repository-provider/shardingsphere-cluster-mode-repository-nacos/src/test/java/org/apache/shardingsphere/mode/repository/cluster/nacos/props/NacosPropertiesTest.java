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

package org.apache.shardingsphere.mode.repository.cluster.nacos.props;

import org.apache.shardingsphere.infra.instance.utils.IpUtils;
import org.junit.Test;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NacosPropertiesTest {
    
    @Test
    public void assertGetValue() {
        NacosProperties actual = new NacosProperties(createProperties());
        assertThat(actual.getValue(NacosPropertyKey.CLUSTER_IP), is("127.0.0.1"));
        assertThat(actual.getValue(NacosPropertyKey.DATA_SOURCE_POOL_CLASS_NAME), is("org.apache.commons.dbcp2.BasicDataSource"));
        assertThat(actual.getValue(NacosPropertyKey.URL), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getValue(NacosPropertyKey.USERNAME), is("sa"));
        assertThat(actual.getValue(NacosPropertyKey.PASSWORD), is("root"));
        assertThat(actual.getValue(NacosPropertyKey.CONNECTION_TIMEOUT_MILLISECONDS), is(10000L));
        assertThat(actual.getValue(NacosPropertyKey.IDLE_TIMEOUT_MILLISECONDS), is(10000L));
        assertThat(actual.getValue(NacosPropertyKey.MAX_LIFETIME_MILLISECONDS), is(1500000L));
        assertThat(actual.getValue(NacosPropertyKey.MAX_POOL_SIZE), is(150));
        assertThat(actual.getValue(NacosPropertyKey.MIN_POOL_SIZE), is(3));
        assertThat(actual.getValue(NacosPropertyKey.INIT_SCHEMA), is(true));
        assertThat(actual.getValue(NacosPropertyKey.RETRY_INTERVAL_MILLISECONDS), is(1000L));
        assertThat(actual.getValue(NacosPropertyKey.MAX_RETRIES), is(5));
        assertThat(actual.getValue(NacosPropertyKey.TIME_TO_LIVE_SECONDS), is(60));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty(NacosPropertyKey.CLUSTER_IP.getKey(), "127.0.0.1");
        result.setProperty(NacosPropertyKey.DATA_SOURCE_POOL_CLASS_NAME.getKey(), "org.apache.commons.dbcp2.BasicDataSource");
        result.setProperty(NacosPropertyKey.URL.getKey(), "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.setProperty(NacosPropertyKey.USERNAME.getKey(), "sa");
        result.setProperty(NacosPropertyKey.PASSWORD.getKey(), "root");
        result.setProperty(NacosPropertyKey.CONNECTION_TIMEOUT_MILLISECONDS.getKey(), "10000");
        result.setProperty(NacosPropertyKey.IDLE_TIMEOUT_MILLISECONDS.getKey(), "10000");
        result.setProperty(NacosPropertyKey.MAX_LIFETIME_MILLISECONDS.getKey(), "1500000");
        result.setProperty(NacosPropertyKey.MAX_POOL_SIZE.getKey(), "150");
        result.setProperty(NacosPropertyKey.MIN_POOL_SIZE.getKey(), "3");
        result.setProperty(NacosPropertyKey.INIT_SCHEMA.getKey(), "true");
        result.setProperty(NacosPropertyKey.RETRY_INTERVAL_MILLISECONDS.getKey(), "1000");
        result.setProperty(NacosPropertyKey.MAX_RETRIES.getKey(), "5");
        result.setProperty(NacosPropertyKey.TIME_TO_LIVE_SECONDS.getKey(), "60");
        return result;
    }
    
    @Test
    public void assertGetDefaultValue() {
        NacosProperties actual = new NacosProperties(new Properties());
        assertThat(actual.getValue(NacosPropertyKey.CLUSTER_IP), is(IpUtils.getIp()));
        assertThat(actual.getValue(NacosPropertyKey.DATA_SOURCE_POOL_CLASS_NAME), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(actual.getValue(NacosPropertyKey.URL), is(""));
        assertThat(actual.getValue(NacosPropertyKey.USERNAME), is("root"));
        assertThat(actual.getValue(NacosPropertyKey.PASSWORD), is(""));
        assertThat(actual.getValue(NacosPropertyKey.CONNECTION_TIMEOUT_MILLISECONDS), is(30000L));
        assertThat(actual.getValue(NacosPropertyKey.IDLE_TIMEOUT_MILLISECONDS), is(60000L));
        assertThat(actual.getValue(NacosPropertyKey.MAX_LIFETIME_MILLISECONDS), is(1800000L));
        assertThat(actual.getValue(NacosPropertyKey.MAX_POOL_SIZE), is(50));
        assertThat(actual.getValue(NacosPropertyKey.MIN_POOL_SIZE), is(1));
        assertThat(actual.getValue(NacosPropertyKey.INIT_SCHEMA), is(false));
        assertThat(actual.getValue(NacosPropertyKey.RETRY_INTERVAL_MILLISECONDS), is(500L));
        assertThat(actual.getValue(NacosPropertyKey.MAX_RETRIES), is(3));
        assertThat(actual.getValue(NacosPropertyKey.TIME_TO_LIVE_SECONDS), is(30));
    }
}
