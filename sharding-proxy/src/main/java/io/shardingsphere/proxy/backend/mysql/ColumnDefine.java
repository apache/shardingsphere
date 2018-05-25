package io.shardingsphere.proxy.backend.mysql;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ColumnDefine {
    @Getter
    private int index;
    @Getter
    private String name;
}
