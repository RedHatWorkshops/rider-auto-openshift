package org.fusesource.camel.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Converter;
import org.fusesource.camel.model.Order;

@Converter
public class OrderConverter {

    @Converter
    public static Order toOrder(ArrayList<Map<String, Object>> csvOrder) {
        Order order = null;
        Map<String, Object> map = csvOrder.get(0);
        for (Entry<String, Object> entry : map.entrySet()) {
            order = (Order) entry.getValue();
            break;
        }
        return order;
    }
}
