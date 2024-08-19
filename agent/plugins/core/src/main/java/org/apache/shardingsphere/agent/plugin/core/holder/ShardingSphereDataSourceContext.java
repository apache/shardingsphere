/*
 * Copyright © 2022，Beijing Sifei Software Technology Co., LTD.
 * All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential
 */

package org.apache.shardingsphere.agent.plugin.core.holder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.manager.ContextManager;

@RequiredArgsConstructor
@Getter
public final class ShardingSphereDataSourceContext {
    
    private final String databaseName;
    
    private final ContextManager contextManager;
}
