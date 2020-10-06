package main.java.node.nodes.bitcoin;

import main.java.node.nodes.MinerNode;
import main.java.simulator.Simulator;

public class BitcoinMinerNode extends BitcoinNode implements MinerNode {
    public BitcoinMinerNode(Simulator simulator, int nodeID, long downloadBandwidth, long uploadBandwidth) {
        super(simulator, nodeID, downloadBandwidth, uploadBandwidth);
    }

    @Override
    public void generateNewBlock() {

    }

    @Override
    public long getHashPower() {
        return 0;
    }
}
