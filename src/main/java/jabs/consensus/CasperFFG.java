package jabs.consensus;

import jabs.blockchain.LocalBlockTree;
import jabs.data.Block;
import jabs.data.BlockWithTx;
import jabs.data.Tx;
import jabs.data.Vote;
import jabs.data.casper.CasperFFGLink;
import jabs.data.casper.CasperFFGVote;
import jabs.message.Packet;
import jabs.message.VoteMessage;
import jabs.node.nodes.Node;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.*;

public class CasperFFG<B extends Block<B>, T extends Tx<T>> extends GhostProtocol<B, T>
        implements VotingBasedConsensus<B, T>, DeterministicFinalityConsensus<B,T> {
    private final HashMap<CasperFFGLink<B>, HashMap<Node, CasperFFGVote<B>>> votes = new HashMap<>();
    private final SortedSet<B> justifiedBlocks = new TreeSet<>();
    private final SortedSet<B> finalizedBlocks = new TreeSet<>();

    private final Set<B> indirectlyFinalizedBlocks = new HashSet<>();
    private final Set<T> finalizedTxs = new HashSet<>();

    private final int checkpointSpace;
    private final int numOfStakeholders;
    private int latestCheckpoint = 0;

    private DescriptiveStatistics blockFinalizationTimes = null;

    public CasperFFG(LocalBlockTree<B> localBlockTree, int checkpointSpace, int numOfStakeholders) {
        super(localBlockTree);
        this.checkpointSpace = checkpointSpace;
        this.numOfStakeholders = numOfStakeholders;
        this.justifiedBlocks.add(localBlockTree.getGenesisBlock());
        this.finalizedBlocks.add(localBlockTree.getGenesisBlock());
        this.indirectlyFinalizedBlocks.add(localBlockTree.getGenesisBlock());
    }

    public void enableFinalizationTimeRecords(DescriptiveStatistics blockFinalizationTimes) {
        this.blockFinalizationTimes = blockFinalizationTimes;
    }

    @Override
    public void newIncomingVote(Vote vote) {
        if (vote instanceof CasperFFGVote) {
            CasperFFGVote<B> casperFFGVote = (CasperFFGVote<B>) vote;
            CasperFFGLink<B> casperFFGLink = casperFFGVote.getLink();
            if (localBlockTree.areBlocksConnected(casperFFGLink.getToBeJustified(), casperFFGLink.getToBeFinalized())) {
                if (!votes.containsKey(casperFFGLink)) { // first vote for the link
                    votes.put(casperFFGLink, new HashMap<>());
                }
                votes.get(casperFFGLink).put(casperFFGVote.getVoter(), casperFFGVote);
                if (votes.get(casperFFGLink).size() > (((numOfStakeholders / 3) * 2) + 1)) {
                    justifiedBlocks.add(casperFFGLink.getToBeJustified());
                    if (!finalizedBlocks.contains(casperFFGLink.getToBeFinalized())) {
                        updateFinalizedBlocks(casperFFGLink.getToBeFinalized());
                        finalizedBlocks.add(casperFFGLink.getToBeFinalized());
                        if (this.originOfGhost.getHeight() < casperFFGLink.getToBeFinalized().getHeight()) {
                            this.originOfGhost = casperFFGLink.getToBeFinalized();
                        }
                    }
                }
            }
        }
    }

    private void updateFinalizedBlocks(B newlyFinalizedBlock) {
        if (!indirectlyFinalizedBlocks.contains(newlyFinalizedBlock)) {
            indirectlyFinalizedBlocks.add(newlyFinalizedBlock);
            if (blockFinalizationTimes != null) {
                blockFinalizationTimes.addValue(blockchainNode.getSimulator().getCurrentTime() - newlyFinalizedBlock.getCreationTime());
            }
        }

        HashSet<B> ancestors = localBlockTree.getAllAncestors(newlyFinalizedBlock);

        for (B block:ancestors) {
            if (!indirectlyFinalizedBlocks.contains(block)) {
                indirectlyFinalizedBlocks.add(block);
                if (blockFinalizationTimes != null) {
                    blockFinalizationTimes.addValue(blockchainNode.getSimulator().getCurrentTime() - block.getCreationTime());
                }
                if (block instanceof BlockWithTx) {
                    finalizedTxs.addAll(((BlockWithTx<T>) block).getTxs());
                }
            }
        }
    }

    @Override
    protected void updateChain() {
        this.acceptedBlocks = this.localBlockTree.getAllAncestors(this.currentMainChainHead);
        if ((currentMainChainHead.getHeight() - checkpointSpace) > latestCheckpoint) {
            latestCheckpoint = currentMainChainHead.getHeight() - (currentMainChainHead.getHeight() % checkpointSpace);

            B toBeFinalizedBlock = finalizedBlocks.last();
            if (localBlockTree.areBlocksConnected(currentMainChainHead, justifiedBlocks.last())) {
                if (finalizedBlocks.last().getHeight() < justifiedBlocks.last().getHeight()) {
                    toBeFinalizedBlock = justifiedBlocks.last();
                }
            } else {
                for (B jBlock:justifiedBlocks) {
                    if (localBlockTree.areBlocksConnected(currentMainChainHead, jBlock)) {
                        if (finalizedBlocks.last().getHeight() < jBlock.getHeight()) {
                            toBeFinalizedBlock = jBlock;
                        }
                    }
                }
            }

            B toBeJustifiedBlock = localBlockTree.getAncestorOfHeight(currentMainChainHead, latestCheckpoint);

            if (!localBlockTree.areBlocksConnected(toBeJustifiedBlock, toBeFinalizedBlock)) {
                System.out.print("What?\n");
            }

            CasperFFGLink<B> casperFFGLink = new CasperFFGLink<>(toBeFinalizedBlock, toBeJustifiedBlock);
            CasperFFGVote<B> casperFFGVote = new CasperFFGVote<>(this.blockchainNode, casperFFGLink);
            VoteMessage voteMessage = new VoteMessage(casperFFGVote);
            Packet packet = new Packet(this.blockchainNode, this.blockchainNode, voteMessage);
            this.blockchainNode.processIncomingPacket(packet);
        }
    }

    public int getNumOfJustifiedBlocks() {
        return this.justifiedBlocks.size();
    }

    @Override
    public boolean isBlockFinalized(B block) {
        return this.indirectlyFinalizedBlocks.contains(block);
    }

    @Override
    public boolean isTxFinalized(T tx) {
        return this.finalizedTxs.contains(tx);
    }

    @Override
    public int getNumOfFinalizedBlocks() {
        return this.indirectlyFinalizedBlocks.size();
    }

    @Override
    public int getNumOfFinalizedTxs() {
        return this.finalizedTxs.size();
    }
}