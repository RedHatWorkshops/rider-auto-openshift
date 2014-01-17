package org.fusesource.camel.test;

import java.util.UUID;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.fusesource.camel.model.Order;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/META-INF/spring/camel-context.xml", 
                       "classpath:/META-INF/spring/test-camel-context.xml"})
@MockEndpoints
public class BackendUnitTest {

  @Autowired
  private CamelContext camelCtx;
  
  @Produce(uri = "activemq:orders")
  private ProducerTemplate amqPT;
  
  @EndpointInject(uri = "mock:activemq:orders")
  private MockEndpoint mockBackendEP;
  
  @Test
  public void testRiderAutoBackendRoute() throws Exception {
    
    String id = UUID.randomUUID().toString();
    Order body = new Order();
    body.setName("foo");
    body.setAmount(5);
    amqPT.sendBodyAndHeader(body, "JMSCorrelationID", id);
    mockBackendEP.expectedMessageCount(1);
    mockBackendEP.expectedHeaderReceived("JMSCorrelationID", id);
    MockEndpoint.assertIsSatisfied(mockBackendEP);
  }
}
