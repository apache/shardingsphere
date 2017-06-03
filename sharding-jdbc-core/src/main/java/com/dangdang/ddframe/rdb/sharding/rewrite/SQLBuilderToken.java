package com.dangdang.ddframe.rdb.sharding.rewrite;

import com.google.common.base.Joiner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * SQL构建器占位符.
 *
 * @author zhangliang
 */
@AllArgsConstructor
@Getter
@Setter
public final class SQLBuilderToken {
    
    private final String label;
    
    private String value;
    
    String toToken() {
        if (null == value) {
            return "";
        }
        return label.equals(value) ? Joiner.on("").join("[Token(", value, ")]") : Joiner.on("").join("[", label, "(", value, ")]");
    }
    
    @Override
    public String toString() {
        return null == value ? "" : value;
    }
}
