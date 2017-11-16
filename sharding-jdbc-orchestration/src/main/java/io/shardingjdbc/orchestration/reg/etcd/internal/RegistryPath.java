package io.shardingjdbc.orchestration.reg.etcd.internal;

import com.google.common.collect.Lists;
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
    private List<String> paths = Lists.newArrayList();

    private RegistryPath(List<String> paths) {
        this.paths.addAll(paths);
    }

    public static RegistryPath from(String... paths) {
        return new RegistryPath(Arrays.asList(paths));
    }

    public RegistryPath join(String path) {
        List<String> newPaths = Lists.newArrayList(paths);
        newPaths.add(path);
        return new RegistryPath(newPaths);
    }

    public String asNodeKey() {
        return StringUtils.join(paths, SEP);
    }

    public String asNodePath() {
        return StringUtils.join(paths, SEP) + "/";
    }
}
