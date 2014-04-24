package org.fusesource.camel.test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/META-INF/spring/camel-context.xml", 
                       "classpath:/META-INF/spring/test-camel-context.xml"})
public class NormalizerUnitTest {

  @Autowired
  private CamelContext camelCtx;
  
  @Produce(uri = "activemq:incomingOrders?disableReplyTo=true")
  private ProducerTemplate amqIncomingOrdersPT;
  
  @EndpointInject(uri = "mock:activemq:orders")
  private MockEndpoint mockAmqOrdersEP;
  
  @EndpointInject(uri = "mock:activemq:invalidOrders")
  private MockEndpoint mockAmqInvalidOrdersEP;
  
  @Before
  public void adviceRoutes() throws Exception {
    ((ModelCamelContext) camelCtx).addRoutes(new RouteBuilder() {

      @Override
      public void configure() throws Exception {
        from("activemq:orders")
          .to(mockAmqOrdersEP);
        
        from("activemq:invalidOrders")
          .to(mockAmqInvalidOrdersEP);
      }
    });
  }
  
  @DirtiesContext
  @Test
  public void testRiderAutoNormalizerXmlRoute() throws Exception {
    
    String id = UUID.randomUUID().toString();
    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put("JMSCorrelationID", id);
    headers.put("CamelFileName", "message.xml");
    String body = "<order><name>motor</name><amount>1</amount></order>";
    amqIncomingOrdersPT.sendBodyAndHeaders(body, headers);
    mockAmqOrdersEP.expectedMessageCount(1);
    mockAmqOrdersEP.expectedHeaderReceived("JMSCorrelationID", id);
    MockEndpoint.assertIsSatisfied(mockAmqOrdersEP);
  }
  
  @DirtiesContext
  @Test
  public void testRiderAutoNormalizerCsvRoute() throws Exception {
    
    String id = UUID.randomUUID().toString();
    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put("JMSCorrelationID", id);
    headers.put("CamelFileName", "message.csv");
    String body = "name,amount\nbrake pad,2";
    amqIncomingOrdersPT.sendBodyAndHeaders(body, headers);
    mockAmqOrdersEP.expectedMessageCount(1);
    mockAmqOrdersEP.expectedHeaderReceived("JMSCorrelationID", id);
    MockEndpoint.assertIsSatisfied(mockAmqOrdersEP);
  }
  
  @DirtiesContext
  @Test
  public void testRiderAutoNormalizerInvalidXmlRoute() throws Exception {
    
    String id = UUID.randomUUID().toString();
    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put("JMSCorrelationID", id);
    headers.put("CamelFileName", "message.xml");
    String body = "<foo></bar>";
    amqIncomingOrdersPT.sendBodyAndHeaders(body, headers);
    mockAmqOrdersEP.expectedMessageCount(0);
    mockAmqInvalidOrdersEP.expectedMessageCount(1);
    mockAmqInvalidOrdersEP.expectedHeaderReceived("JMSCorrelationID", id);
    MockEndpoint.assertIsSatisfied(mockAmqOrdersEP, mockAmqInvalidOrdersEP);
  }
}
