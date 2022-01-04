package org.apache.shardingsphere.traffic.executor.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Statement;

/**
 * Traffic executor context.
 */
@RequiredArgsConstructor
@Getter
public final class TrafficExecutorContext<T extends Statement> {
    
    private final T statement;
}
