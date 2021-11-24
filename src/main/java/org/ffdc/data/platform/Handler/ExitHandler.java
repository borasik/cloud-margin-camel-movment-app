package org.ffdc.data.platform.Handler;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExitHandler {
	private static final Logger LOG = LoggerFactory.getLogger(ExitHandler.class);
	
    public void errorReport(Exchange exchange) {
    	if(exchange.getException() != null) {
        	LOG.error("Error Occured {}", exchange.getException().getMessage());
    	}
    	else {
    		Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
    		LOG.error("Error Occured {}", cause.getMessage());
    		String stacktrace = ExceptionUtils.getStackTrace(cause);
    		LOG.error("Error Trace {}", stacktrace);
    	}
    }
            
    public void shutdown(Exchange exchange) {
        if (!alreadyShuttingDown(exchange)) {
            shutdownCamel(exchange);
        }
    }

    private boolean alreadyShuttingDown(Exchange exchange) {
        return exchange.getContext().isStopping() || exchange.getContext().isStopped();
    }

    private void shutdownCamel(Exchange exchange) {
        exchange.getContext().getShutdownStrategy().setLogInflightExchangesOnTimeout(false);
        exchange.getContext().getShutdownStrategy().setTimeout(1);
        exchange.getContext().getShutdownStrategy().setShutdownNowOnTimeout(true);
        exchange.getContext().shutdown();
    }

}
