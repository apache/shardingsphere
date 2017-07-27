package com.dangdang.ddframe.rdb.sharding.id.generator.self;

import com.dangdang.ddframe.rdb.sharding.id.generator.IdGenerator;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 浏览 {@link IPIdGenerator} workerId生成的规则后，感觉对服务器IP后10位（特别是IPV6）数值比较约束。
 * 有以下优化思路：
 * 因为workerId最大限制是2^10，我们生成的workerId只要满足小于最大workerId即可。
 * 1.针对IPV4:
 * ....IP最大 255.255.255.255。而（255+255+255+255) < 1024。
 * ....因此采用IP段数值相加即可生成唯一的workerId，不受IP位限制。
 * 2.针对IPV6:
 * ....IP最大ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff
 * ....为了保证相加生成出的workerId < 1024,思路是将每个bit位的后6位相加。这样在一定程度上也可以满足workerId不重复的问题。
 *
 * @author DogFc
 */
public class OptimizeIPIdGenerator implements IdGenerator {

    private final CommonSelfIdGenerator commonSelfIdGenerator = new CommonSelfIdGenerator();

    static {
        initWorkerId();
    }

    static void initWorkerId() {
        InetAddress address;
        try {
            address = InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            throw new IllegalStateException("Cannot get LocalHost InetAddress, please check your network!");
        }
        byte[] ipAddressByteArray = address.getAddress();
        long workerId = 0L;
        //IPV4
        if (ipAddressByteArray.length == 4) {
            for (byte byteNum : ipAddressByteArray) {
                workerId += (byteNum & 0xFF);
            }
            //IPV6
        } else if (ipAddressByteArray.length == 16) {
            for (byte byteNum : ipAddressByteArray) {
                workerId += (byteNum & 0B111111);
            }
        } else {
            throw new IllegalStateException("Bad LocalHost InetAddress, please check your network!");
        }
        CommonSelfIdGenerator.setWorkerId(workerId);
    }

    @Override
    public Number generateId() {
        return commonSelfIdGenerator.generateId();
    }
}
