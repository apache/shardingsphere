package org.apache.shardingsphere.infra.util.reflect;

import lombok.*;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ReflectiveUtilTest {

    @Test
    public void assertGetFieldValue() throws IllegalAccessError {
        ReflectiveFixture reflectiveFixture = new ReflectiveFixture("bar");
        assertThat(ReflectiveUtil.getFieldValue(reflectiveFixture, "value"), is("bar"));
    }

    @Test
    public void assertSetField() throws IllegalAccessError {
        ReflectiveFixture reflectiveFixture = new ReflectiveFixture();
        ReflectiveUtil.setField(reflectiveFixture, "value", "foo");
        assertThat(ReflectiveUtil.getFieldValue(reflectiveFixture, "value"), is("foo"));
    }

    @Test
    public void assertSetStaticField() throws IllegalAccessError {
        ReflectiveFixture reflectiveFixture = new ReflectiveFixture();
        ReflectiveUtil.setStaticField(reflectiveFixture.getClass(), "staticValue", "foo");
        assertThat(ReflectiveUtil.getFieldValue(reflectiveFixture, "staticValue"), is("foo"));
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static final class ReflectiveFixture {
        @Getter
        @Setter(AccessLevel.PRIVATE)
        private String value;
        private static String staticValue;
    }
}
