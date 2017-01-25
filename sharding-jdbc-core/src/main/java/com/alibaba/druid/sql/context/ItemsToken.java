package com.alibaba.druid.sql.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * 选择项语言标记对象.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class ItemsToken implements SQLToken {
    
    private final int beginPosition;
    
    private final List<String> items = new LinkedList<>();
}
