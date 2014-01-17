package org.fusesource.camel.impl;

import org.fusesource.camel.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackEndImpl {
    private static final Logger LOG = LoggerFactory.getLogger(BackEndImpl.class);
    
    public void doWork(Order order) {
        LOG.info("Received order for {} {}s.", order.getAmount(), order.getName());
    }
}
