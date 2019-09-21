package org.apache.shardingsphere.shardingproxy;

import org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.hint.HintShardingValue;

import java.util.Collection;
import java.util.Collections;

/**
 * Class desc.
 *
 * @author liya
 */
public class KK implements HintShardingAlgorithm<String> {
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, HintShardingValue<String> shardingValue) {
        System.out.println("=====:"+availableTargetNames);
        System.out.println("=====:"+shardingValue.getValues());
        if(shardingValue.getValues().size()==1 && shardingValue.getValues().iterator().next().equals("cookie")){
            return Collections.singleton("user_0");
        }
        if(shardingValue.getValues().size()==1 && shardingValue.getValues().iterator().next().equals("liya")){
            return Collections.singleton("user_1");
        }
        return availableTargetNames;
    }
}
