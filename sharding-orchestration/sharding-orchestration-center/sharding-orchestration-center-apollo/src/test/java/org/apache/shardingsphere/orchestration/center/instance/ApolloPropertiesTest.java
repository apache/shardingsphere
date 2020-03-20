package org.apache.shardingsphere.orchestration.center.instance;

import org.junit.Test;
import java.util.Properties;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ApolloPropertiesTest {

    @Test
    public void assertGetValue() {
        Properties props = new Properties();
        props.setProperty(ApolloPropertyKey.APP_ID.getKey(),"APOLLO_SHARDING_SPHERE1");
        props.setProperty(ApolloPropertyKey.ENV.getKey(),"PROD");
        props.setProperty(ApolloPropertyKey.CLUSTER_NAME.getKey(),"sit");
        props.setProperty(ApolloPropertyKey.ADMINISTRATOR.getKey(),"guest");
        props.setProperty(ApolloPropertyKey.TOKEN.getKey(),"a6e6bbec-1eed-4ddc-bb10-6791beb982d9");
        props.setProperty(ApolloPropertyKey.PORTAL_URL.getKey(),"http://apollo.ctrip.sit");
        props.setProperty(ApolloPropertyKey.CONNECT_TIMEOUT.getKey(),"3000");
        props.setProperty(ApolloPropertyKey.READ_TIMEOUT.getKey(),"6000");
        ApolloProperties actual = new ApolloProperties(props);
        assertThat(actual.getValue(ApolloPropertyKey.APP_ID),is("APOLLO_SHARDING_SPHERE1"));
        assertThat(actual.getValue(ApolloPropertyKey.ENV),is("PROD"));
        assertThat(actual.getValue(ApolloPropertyKey.CLUSTER_NAME),is("sit"));
        assertThat(actual.getValue(ApolloPropertyKey.ADMINISTRATOR),is("guest"));
        assertThat(actual.getValue(ApolloPropertyKey.TOKEN),is("a6e6bbec-1eed-4ddc-bb10-6791beb982d9"));
        assertThat(actual.getValue(ApolloPropertyKey.PORTAL_URL),is("http://apollo.ctrip.sit"));
        assertThat(actual.getValue(ApolloPropertyKey.CONNECT_TIMEOUT),is(3000));
        assertThat(actual.getValue(ApolloPropertyKey.READ_TIMEOUT),is(6000));
    }

    @Test
    public void assertGetDefaultValue() {
        ApolloProperties actual = new ApolloProperties(new Properties());
        assertThat(actual.getValue(ApolloPropertyKey.APP_ID),is("APOLLO_SHARDING_SPHERE"));
        assertThat(actual.getValue(ApolloPropertyKey.ENV),is("DEV"));
        assertThat(actual.getValue(ApolloPropertyKey.CLUSTER_NAME),is("default"));
        assertThat(actual.getValue(ApolloPropertyKey.ADMINISTRATOR),is(""));
        assertThat(actual.getValue(ApolloPropertyKey.TOKEN),is(""));
        assertThat(actual.getValue(ApolloPropertyKey.PORTAL_URL),is(""));
        assertThat(actual.getValue(ApolloPropertyKey.CONNECT_TIMEOUT),is(1000));
        assertThat(actual.getValue(ApolloPropertyKey.READ_TIMEOUT),is(5000));
    }
}