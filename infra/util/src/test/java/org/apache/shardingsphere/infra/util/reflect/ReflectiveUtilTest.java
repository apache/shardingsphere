package org.apache.shardingsphere.infra.util.reflect;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ReflectiveUtilTest {

    @Test
    public void testGetFieldValue() {
        UserPojo pojo = new UserPojo("test");
        String fieldValue = (String)ReflectiveUtil.getFieldValue(pojo, "foo");
        assertTrue("test".equals(fieldValue));
    }

    @Test
    public void testSetField() {
        UserPojo pojo = new UserPojo();
        ReflectiveUtil.setField(pojo, "foo", "test");
        assertTrue("test".equals(pojo.getFoo()));
    }

    @Test
    public void testSetStaticField() {
        ReflectiveUtil.setStaticField(UserPojo.class, "bar", "test");
        assertTrue("test".equals(UserPojo.bar));
    }

    @AllArgsConstructor
    @NoArgsConstructor
    static class UserPojo {

        @Getter
        private String foo;

        private static String bar;
    }
}
