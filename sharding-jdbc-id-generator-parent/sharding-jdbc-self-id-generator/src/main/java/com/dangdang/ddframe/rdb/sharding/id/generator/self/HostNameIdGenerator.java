package com.dangdang.ddframe.rdb.sharding.id.generator.self;

import com.dangdang.ddframe.rdb.sharding.id.generator.IdGenerator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * .
 * 根据机器名最后的数字编号获取工作进程Id，如果线上机器命名有统一规范，建议使用此种方式
 * ，列如机器的HostName为
 * ，dangdang-db-sharding-dev-01（公司名-部门名-服务名-环境名-编号）
 * ，会截取HostName最后的编号01作为workerId。
 *
 * @author DonneyYoung
 **/
@Getter
@Slf4j
public class HostNameIdGenerator implements IdGenerator {

    private static final CommonSelfIdGenerator COMMON_SELF_ID_GENERATOR;

    static {
        COMMON_SELF_ID_GENERATOR = new CommonSelfIdGenerator();
        initWorkerId();
    }

    static void initWorkerId() {
        InetAddress addr;
        Long workerId;
        try {
            addr = InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            throw new IllegalStateException("Cannot get LocalHost InetAddress , please check your network!");
        }
        try {
            String hostName = addr.getHostName();
            workerId = Long.valueOf(hostName.replace(hostName.replaceAll("\\d+$", ""), ""));
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Wrong hostname , hostname must be end with number!");
        }
        CommonSelfIdGenerator.setWorkerId(workerId);
    }

    @Override
    public Number generateId() {
        return COMMON_SELF_ID_GENERATOR.generateId();
    }
}
