package org.apache.shardingsphere.sharding.algorithm.sharding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.junit.Before;
import org.junit.Test;

public class ShardingAutoTableAlgorithmUtilTest {

    private Collection<String> collection;
    private DataNodeInfo dataNodeInfo;

    @Before
    public void setup(){
        collection = new ArrayList<>();
        collection.add("PREFIX----SUFFIX");
        collection.add("PREFIXSUFFIXSTRING");
        collection.add("PREFIX----------");

        final String prefix = "PREFIX";
        final int suffixMinLength = 10;
        final char paddingChar = '-';
        dataNodeInfo = new DataNodeInfo(prefix, suffixMinLength, paddingChar);
    }

    @Test
    public void assertFindMatchedTargetNameForValidInputs(){

        Optional<String> output = ShardingAutoTableAlgorithmUtil.findMatchedTargetName(collection,"SUFFIX",dataNodeInfo);
        assert(output.isPresent());
        assertEquals("PREFIX----SUFFIX", output.get());

        Optional<String> output1 = ShardingAutoTableAlgorithmUtil.findMatchedTargetName(collection,"SUFFIXSTRING",dataNodeInfo);
        assert(output1.isPresent());
        assertEquals("PREFIXSUFFIXSTRING", output1.get());

        Optional<String> output2 = ShardingAutoTableAlgorithmUtil.findMatchedTargetName(collection,"",dataNodeInfo);
        assert(output2.isPresent());
        assertEquals("PREFIX----------", output2.get());
    }

    @Test
    public void assertFindMatchedTargetNameNonExistingInput(){
        Optional<String> output = ShardingAutoTableAlgorithmUtil.findMatchedTargetName(collection,"NONEXISTINGSUFFIX",dataNodeInfo);
        assertFalse(output.isPresent());
    }
}
