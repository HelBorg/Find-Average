package ru.spbu.bh;


import jade.core.Agent;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Data
public class DefaultAgent extends Agent {

    private List<String> linkedAgents;
    private int number;

    // Sum of all received numbers
    private Double sum;

    // Agents whose numbers was already added
    private List<String> numProcessedAgents;

    @Override
    protected void setup() {
        int id = getId();

        Object[] arguments = getArguments();
        if (arguments != null && arguments[0] instanceof String[]) {
            linkedAgents = Arrays.asList((String[]) arguments[0]);
        } else {
            linkedAgents = new ArrayList<>();
        }

        Random rand = new Random();
        number = rand.nextInt(50);

        sum = (double) number;
        numProcessedAgents = new ArrayList<String>() {{
            add(getLocalName());
        }};

        System.out.println("Agent " + id + " with random number " + number);

        addBehaviour(new FindAverage(this, TimeUnit.SECONDS.toMillis(1)));
    }

    public int getId() {
        return Integer.parseInt(getAID().getLocalName().substring(0, 1));
    }

    public void addToSum(Integer add) {
        this.sum += add;
    }

    public double getAVG() {
        return this.sum / this.getNumProcessedAgents().size();
    }

}
