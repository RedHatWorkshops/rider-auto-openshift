package org.fusesource.camel.test;

import java.util.UUID;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.test.junit4.CamelSpringJUnit4ClassRunner;
import org.fusesource.camel.model.Order;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/META-INF/spring/camel-context.xml", 
                       "classpath:/META-INF/spring/test-camel-context.xml"})
public class WsUnitTest {

  @Autowired
  private CamelContext camelCtx;
  
  @Produce(uri = "cxf:bean:orderEndpoint")
  private ProducerTemplate wsOrderPT;
  
  @EndpointInject(uri = "mock:activemq:incomingOrders")
  private MockEndpoint mockAmqIncomingOrdersEP;
  
  @Before
  public void adviceRoutes() throws Exception {
    ((ModelCamelContext) camelCtx).addRoutes(new RouteBuilder() {

      @Override
      public void configure() throws Exception {
        from("activemq:incomingOrders")
          .to(mockAmqIncomingOrdersEP);
      }
    });
  }
  
  @Test
  public void testRiderAutoWsRoute() throws Exception {
    
    String id = UUID.randomUUID().toString();
    Order body = new Order();
    body.setName("foo");
    body.setAmount(5);
    wsOrderPT.sendBodyAndHeader(body, "JMSCorrelationID", id);
    mockAmqIncomingOrdersEP.expectedMessageCount(1);
    mockAmqIncomingOrdersEP.expectedHeaderReceived("JMSCorrelationID", id);
    MockEndpoint.assertIsSatisfied(mockAmqIncomingOrdersEP);
  }
}
