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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/META-INF/spring/camel-context.xml", 
                       "classpath:/META-INF/spring/test-camel-context.xml"})
public class FilePollerUnitTest {

  @Autowired
  private CamelContext camelCtx;
  
  @Produce(uri = "file:target/placeorder")
  private ProducerTemplate filePT;
  
  @EndpointInject(uri = "mock:activemq:incomingOrders")
  private MockEndpoint mockAmqOrderEP;
  
  @Before
  public void adviceRoutes() throws Exception {
    ((ModelCamelContext) camelCtx).addRoutes(new RouteBuilder() {

      @Override
      public void configure() throws Exception {
        from("activemq:incomingOrders")
          .to(mockAmqOrderEP);
      }
    });
  }
  
  @Test
  public void testRiderAutoFilePollerRoute() throws Exception {
    
    String id = UUID.randomUUID().toString();
    String fname = id + ".txt";
    filePT.sendBodyAndHeader(id, "CamelFileName", fname);
    mockAmqOrderEP.expectedMessageCount(1);
    mockAmqOrderEP.expectedBodiesReceived(id);
    MockEndpoint.assertIsSatisfied(mockAmqOrderEP);
  }
}
