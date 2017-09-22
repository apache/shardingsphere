package io.shardingjdbc.example.jpa.service;

import io.shardingjdbc.example.jpa.entity.Order;
import io.shardingjdbc.example.jpa.repository.OrderRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    
    @Resource
    private OrderRepository orderRepository;
    
    public void testAll() {
        List<Long> orderIds = new ArrayList<>(10);
        System.out.println("1.Insert--------------");
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setUserId(51);
            order.setStatus("INSERT_TEST");
            orderIds.add(orderRepository.insert(order));
        }
        System.out.println(orderRepository.selectAll());
        System.out.println("2.Delete--------------");
        for (Long each : orderIds) {
            orderRepository.delete(each);
        }
        System.out.println(orderRepository.selectAll());
    }
}
