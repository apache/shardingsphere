package io.shardingjdbc.example.spring.boot.jpa.service;

import io.shardingjdbc.example.spring.boot.jpa.repository.OrderItemRepository;
import io.shardingjdbc.example.spring.boot.jpa.entity.Order;
import io.shardingjdbc.example.spring.boot.jpa.entity.OrderItem;
import io.shardingjdbc.example.spring.boot.jpa.repository.OrderRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class DemoService {
    
    @Resource
    private OrderRepository orderRepository;
    
    @Resource
    private OrderItemRepository orderItemRepository;
    
    public void demo() {
        List<Long> orderIds = new ArrayList<>(10);
        List<Long> orderItemIds = new ArrayList<>(10);
        System.out.println("1.Insert--------------");
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setUserId(51);
            order.setStatus("INSERT_TEST");
            long orderId = orderRepository.save(order).getOrderId();
            orderIds.add(orderId);
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setUserId(51);
            item.setStatus("INSERT_TEST");
            orderItemIds.add(orderItemRepository.save(item).getOrderItemId());
        }
        List<OrderItem> orderItems = orderItemRepository.findAll();
        System.out.println(orderItemRepository.findAll());
        System.out.println("2.Delete--------------");
        if (orderItems.size() > 0) {
            for (Long each : orderItemIds) {
                orderItemRepository.delete(each);
            }
            for (Long each : orderIds) {
                orderRepository.delete(each);
            }
        }
        System.out.println(orderItemRepository.findAll());
    }
}
