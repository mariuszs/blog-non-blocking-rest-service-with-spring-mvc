package se.callista.springmvc.asynch.routingslip.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import se.callista.springmvc.asynch.routingslip.statemachine.Processor;
import se.callista.springmvc.asynch.routingslip.statemachine.State;
import se.callista.springmvc.asynch.routingslip.statemachine.StateMachine;

/**
 * Created by magnus on 15/05/14.
 */
@Component
public class SynchProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(SynchProcessor.class);

    @Autowired
    private StateMachine stateMachine;

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${sp.blocking.url}")
    private String SP_BLOCKING_URL;

    @Override
    public void process(State state) {

        int sleeptimeMs = 100 * (state.sequenceNo);
        String url = SP_BLOCKING_URL + "?minMs=" + sleeptimeMs + "&maxMs=" + sleeptimeMs;

        LOG.debug("Launch synch call");

        ResponseEntity<String> result = restTemplate.getForEntity(
            SP_BLOCKING_URL + "?minMs={minMs}&maxMs={maxMs}", String.class, sleeptimeMs, sleeptimeMs);

        // TODO: Handle status codes other than 200...
        HttpStatus status = result.getStatusCode();

        LOG.debug("Synch call complete, hand over to the state machine for the next action");
        state.temporaryResult += result.getBody() + '\n';
        stateMachine.processStateComplete(state);
    }
}
