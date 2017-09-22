package io.shardingjdbc.example.mybatis.service;

import io.shardingjdbc.example.mybatis.entity.Order;
import io.shardingjdbc.example.mybatis.repository.OrderRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    
    @Resource
    private OrderRepository orderRepository;
    
    public void testAll() {
        orderRepository.dropTable();
        orderRepository.createTable();
        List<Long> orderIds = new ArrayList<>(10);
        System.out.println("1.Insert--------------");
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setUserId(51);
            order.setStatus("INSERT_TEST");
            orderRepository.insert(order);
            orderIds.add(order.getOrderId());
        }
        System.out.println(orderRepository.selectAll());
        System.out.println("2.Delete--------------");
        for (Long each : orderIds) {
            orderRepository.delete(each);
        }
        System.out.println(orderRepository.selectAll());
    }
}