/*
 * Copyright © 2022，Beijing Sifei Software Technology Co., LTD.
 * All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential
 */

package org.apache.shardingsphere.integration.data.pipeline.util;

import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public final class AutoIncrementKeyGenerateAlgorithm implements KeyGenerateAlgorithm {
    
    private final AtomicLong idGen = new AtomicLong(1);
    
    @Override
    public Comparable<?> generateKey() {
        return idGen.getAndIncrement();
    }
    
    @Override
    public Properties getProps() {
        return null;
    }
    
    @Override
    public void init(final Properties props) {
    }
}
