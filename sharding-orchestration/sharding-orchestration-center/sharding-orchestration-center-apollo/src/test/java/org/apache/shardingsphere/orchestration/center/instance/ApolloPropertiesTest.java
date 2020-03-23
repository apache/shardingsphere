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

package org.apache.shardingsphere.orchestration.center.instance;

import org.junit.Test;
import java.util.Properties;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ApolloPropertiesTest {

    @Test
    public void assertGetValue() {
        Properties props = new Properties();
        props.setProperty(ApolloPropertyKey.APP_ID.getKey(), "APOLLO_SHARDINGSPHERE1");
        props.setProperty(ApolloPropertyKey.ENV.getKey(), "PROD");
        props.setProperty(ApolloPropertyKey.CLUSTER_NAME.getKey(), "sit");
        props.setProperty(ApolloPropertyKey.ADMINISTRATOR.getKey(), "guest");
        props.setProperty(ApolloPropertyKey.TOKEN.getKey(), "a6e6bbec-1eed-4ddc-bb10-6791beb982d9");
        props.setProperty(ApolloPropertyKey.PORTAL_URL.getKey(), "http://config-service-url");
        props.setProperty(ApolloPropertyKey.CONNECT_TIMEOUT.getKey(), "3000");
        props.setProperty(ApolloPropertyKey.READ_TIMEOUT.getKey(), "6000");
        ApolloProperties actual = new ApolloProperties(props);
        assertThat(actual.getValue(ApolloPropertyKey.APP_ID), is("APOLLO_SHARDINGSPHERE1"));
        assertThat(actual.getValue(ApolloPropertyKey.ENV), is("PROD"));
        assertThat(actual.getValue(ApolloPropertyKey.CLUSTER_NAME), is("sit"));
        assertThat(actual.getValue(ApolloPropertyKey.ADMINISTRATOR), is("guest"));
        assertThat(actual.getValue(ApolloPropertyKey.TOKEN), is("a6e6bbec-1eed-4ddc-bb10-6791beb982d9"));
        assertThat(actual.getValue(ApolloPropertyKey.PORTAL_URL), is("http://config-service-url"));
        assertThat(actual.getValue(ApolloPropertyKey.CONNECT_TIMEOUT), is(3000));
        assertThat(actual.getValue(ApolloPropertyKey.READ_TIMEOUT), is(6000));
    }

    @Test
    public void assertGetDefaultValue() {
        ApolloProperties actual = new ApolloProperties(new Properties());
        assertThat(actual.getValue(ApolloPropertyKey.APP_ID), is("APOLLO_SHARDING_SPHERE"));
        assertThat(actual.getValue(ApolloPropertyKey.ENV), is("DEV"));
        assertThat(actual.getValue(ApolloPropertyKey.CLUSTER_NAME), is("default"));
        assertThat(actual.getValue(ApolloPropertyKey.ADMINISTRATOR), is(""));
        assertThat(actual.getValue(ApolloPropertyKey.TOKEN), is(""));
        assertThat(actual.getValue(ApolloPropertyKey.PORTAL_URL), is(""));
        assertThat(actual.getValue(ApolloPropertyKey.CONNECT_TIMEOUT), is(1000));
        assertThat(actual.getValue(ApolloPropertyKey.READ_TIMEOUT), is(5000));
    }
}
