package io.shardingjdbc.orchestration.reg.etcd.internal;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * RegistryPath
 *
 * @author junxiong
 */
public class RegistryPath {
    private static final String SEP = "/";
    private List<String> paths;

    private RegistryPath(List<String> paths) {
        this.paths = paths;
    }

    public static RegistryPath from(String... paths) {
        return new RegistryPath(Arrays.asList(paths));
    }

    public RegistryPath join(String path) {
        paths.add(path);
        return this;
    }

    public String asNodeKey() {
        return StringUtils.join(paths, SEP);
    }

    public String asNodePath() {
        return StringUtils.join(paths, SEP) + "/";
    }
}
