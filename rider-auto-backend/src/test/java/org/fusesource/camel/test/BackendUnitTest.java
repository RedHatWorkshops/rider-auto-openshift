package org.fusesource.camel.test;

import java.util.UUID;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.fusesource.camel.model.Order;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/META-INF/spring/camel-context.xml",
                       "classpath:/META-INF/spring/test-camel-context.xml"})
public class BackendUnitTest {

  @Autowired
  private CamelContext camelCtx;

  @Produce(uri = "activemq:orders")
  private ProducerTemplate amqPT;

  @EndpointInject(uri = "mock:bean:backendImpl")
  private MockEndpoint mockBackendEP;

  @Before
  public void adviceRoutes() throws Exception {
    ((ModelCamelContext) camelCtx).getRouteDefinition("rider-auto-backend-route").adviceWith((ModelCamelContext) camelCtx, new AdviceWithRouteBuilder() {

      @Override
      public void configure() throws Exception {
        weaveAddLast().to(mockBackendEP);
      }
    });
  }

  @Test
  public void testRiderAutoBackendRoute() throws Exception {

    String id = UUID.randomUUID().toString();
    String body = "<order><name>foo</name><amount>5</amount></order>";
    amqPT.sendBodyAndHeader(body, "JMSCorrelationID", id);
    mockBackendEP.expectedMessageCount(1);
    mockBackendEP.expectedHeaderReceived("JMSCorrelationID", id);
    MockEndpoint.assertIsSatisfied(mockBackendEP);
  }
}
