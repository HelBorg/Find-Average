package ru.spbu.bh;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.java.Log;
import org.apache.commons.collections4.MapUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Log
public class FindAverage extends TickerBehaviour {

    private int currentStep;
    private final int MAX_STEPS = 10;
    private final int SEND_MY_NUM = 1;

    public FindAverage(DefaultAgent agent, long period) {
        super(agent, period);
        this.currentStep = 0;
    }

    @Override
    protected void onTick() {
        if (currentStep >= MAX_STEPS) {
            this.stop((DefaultAgent) this.myAgent);
            return;
        }
        this.currentStep++;

        Map<String, Integer> contentToSend = receiveData();

        if (MapUtils.isEmpty(contentToSend) && currentStep > SEND_MY_NUM) {
            this.stop((DefaultAgent) myAgent);
            return;
        }

        processReceivedData(contentToSend, (DefaultAgent) myAgent);

        if (currentStep <= SEND_MY_NUM) {
            contentToSend.put(myAgent.getLocalName(), ((DefaultAgent) myAgent).getNumber());
        }

        sendData(contentToSend);
    }

    private void sendData(Map<String, Integer> contentToSend) {
        List<String> linkedAgents = ((DefaultAgent) myAgent).getLinkedAgents();
        if (linkedAgents == null) {
            return;
        }

        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        for (String i : linkedAgents) {
            message.addReceiver(new AID(i, AID.ISLOCALNAME));
        }
        try {
            message.setContentObject((Serializable) contentToSend);
        } catch (Exception e) {
            log.warning("Unable to generate message: " + contentToSend);
            e.printStackTrace();
        }
        myAgent.send(message);
    }

    private Map<String, Integer> receiveData() {
        Map<String, Integer> contentToSend = new HashMap<>();
        int linkedAgentsLength = ((DefaultAgent) myAgent).getLinkedAgents().size();
        for (int i = 0; i < linkedAgentsLength; i++) {
            ACLMessage msgRes = myAgent.receive();
            if (msgRes == null) {
                continue;
            }

            try {
                Object receivedContent = msgRes.getContentObject();
                if (receivedContent instanceof Map) {
                    contentToSend.putAll((Map) receivedContent);
                }
            } catch (Exception e) {
                log.warning("Invalid message content in received message" + msgRes);
                e.printStackTrace();
            }
        }
        return contentToSend;
    }

    private void processReceivedData(Map<String, Integer> content, DefaultAgent agent) {
        if (MapUtils.isEmpty(content)) {
            return;
        }
        List<String> numProcessedAgents = agent.getNumProcessedAgents();

        for (Map.Entry<String, Integer> entry : new HashSet<>(content.entrySet())) {
            String key = entry.getKey();

            /* Remove if already processed so that it won't go further */
            if (numProcessedAgents.contains(key)) {
                content.remove(key);
                continue;
            }

            agent.addToSum(entry.getValue());
            numProcessedAgents.add(key);
        }
    }

    private void stop(DefaultAgent currentAgent) {
        if (currentAgent.getName().contains("Main")) {
            System.out.println("\n  AVG - " + currentAgent.getAVG() + "\n");
        }

        currentAgent.doDelete();
        this.stop();
    }
}
