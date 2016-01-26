package com.dangdang.ddframe.rdb.sharding.example.jdbc.service;

import javax.annotation.Resource;

import com.dangdang.ddframe.rdb.sharding.example.jdbc.entity.Order;
import com.dangdang.ddframe.rdb.sharding.example.jdbc.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Order 服务对象.
 * 
 * @author gaohongtao
 */
@Service
@Transactional
public class OrderService {
    
    @Resource
    OrderRepository orderRepository;
    
    @Transactional(readOnly = true)
    public void select() {
        System.out.println(orderRepository.selectAll());
    }
    
    public void clear(){
        orderRepository.deleteAll();
    }
    
    public void add(){
        Order criteria = new Order();
        criteria.setUserId(10);
        criteria.setOrderId(1000);
        criteria.setStatus("INSERT");
        orderRepository.insert(criteria);
    }
    
    public void addRollback(){
        Order criteria = new Order();
        criteria.setUserId(12);
        criteria.setOrderId(1002);
        criteria.setStatus("INSERT2");
        orderRepository.insert(criteria);
        throw new IllegalArgumentException("failed");
    }
}
