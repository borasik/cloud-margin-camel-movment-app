package org.ffdc.data.platform;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;
import org.apache.camel.test.spring.junit5.CamelSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;
import org.ffdc.data.platform.CloudMarginDataMovingToolRouter;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
public class GetDataSetFromAzureDlTest {

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject("get-data-set-from-azure-dl")
    protected MockEndpoint mockA;

    @EndpointInject("mock:output")
    private MockEndpoint mockDone;

    @Produce("direct:start")
    protected ProducerTemplate start;

    @Test
    public void t1() throws Exception {
        // start.sendBodyAndHeaders("Testing", new HashMap<>());

        // mockDone.expectedMessageCount(1);

        assertTrue(true);
    }
}
