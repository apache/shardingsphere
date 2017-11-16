package io.shardingjdbc.orchestration.reg;

import io.shardingjdbc.orchestration.reg.etcd.internal.RegistryPath;
import org.hamcrest.core.IsNot;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RegistryPathTest {

    @Test
    public void testFrom() {
        String key = RegistryPath.from("a", "b", "c").asNodeKey();
        assertThat(key, is("a/b/c"));
    }

    @Test
    public void testNodeKey() {
        RegistryPath root = RegistryPath.from("");
        String key = root.join("test").join("name").join("config").asNodeKey();
        assertThat(key, is("/test/name/config"));
    }

    @Test
    public void testNodePath() {
        RegistryPath root = RegistryPath.from("");
        String key = root.join("test").join("name").join("config").asNodePath();
        assertThat(key, is("/test/name/config/"));
    }

    @Test
    public void testImmutable() {
        RegistryPath root = RegistryPath.from("");
        String keyA = root.join("test").asNodeKey();
        String keyB = root.join("prod").asNodeKey();
        assertThat(keyA, is("/test"));
        assertThat(keyB, is("/prod"));
    }
}
