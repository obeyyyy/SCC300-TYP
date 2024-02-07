package jabs.consensus.algorithm;
import java.util.Map;
import java.util.Set;

import jabs.ledgerdata.ethereum.EthereumAccount;
import jabs.network.networks.Network;
import jabs.network.networks.sharded.PBFTShardedNetwork;
import jabs.network.node.nodes.Node;
import jabs.network.node.nodes.pbft.PBFTShardedNode;
import jabs.simulator.event.MigrationEvent;

// ThresholdMigrationPolicy implementation
public class ThresholdMigrationPolicy implements MigrationPolicy {
     private final int migrationThreshold;
    private PBFTShardedNetwork network;
    private Set<EthereumAccount> accountsInMigration;
    private Node node;
   // public int MigrationCount = 0;

    public ThresholdMigrationPolicy(int migrationThreshold,  Network network, Set<EthereumAccount> accountsInMigration, Node node) {
        this.migrationThreshold = migrationThreshold;
        this.accountsInMigration = accountsInMigration;
        this.node = node;
        this.network = (PBFTShardedNetwork) node.getNetwork();
       // this.MigrationCount = 0;
    }
    

    @Override
public void migrateIfNecessary(EthereumAccount account, EthereumAccount receiver, EthereumAccount sender, Map<String, Integer> crossShardTransactionCount) {
    // Get the unique identifier for the transaction involving both sender and receiver
    String transactionKey = sender.getShardNumber() + "-" + receiver.getShardNumber();

    // Get the current count for the unique identifier
    int accountCrossShardCount = crossShardTransactionCount.getOrDefault(transactionKey, 0);

    // Print debug information
    System.out.println("Account: " + account);
    System.out.println("receiver : " + receiver);
    System.out.println("sender :" + sender);
    System.out.println("Current cross-shard count: " + accountCrossShardCount);

    
    
    // Check if the migration threshold is reached for the current account
    if (accountCrossShardCount >= migrationThreshold) {
        System.out.println("5555555555555555555555555555");
        System.out.println("Sender: " + sender.getShardNumber() + " Receiver :" + receiver.getShardNumber());
        migrateAccount(account, receiver, sender);

        // Reset the count after migration
        crossShardTransactionCount.put(transactionKey, 1);
    }
}




    @Override
    public void migrateAccount(EthereumAccount accounts, EthereumAccount receiverAccount, EthereumAccount currentAccount) {
       // MigrationCount++;
        ((PBFTShardedNetwork)network).MigrationCounts =   ((PBFTShardedNetwork)network).MigrationCounts + 1;
      // newShard = network.getRandomAccount(true).getShardNumber(); // random shard to send the account to for now, soon need to send to only the shards that are in for cross-shard transactions
       // network.accountToShard.put(currentAccount, receiverAccount.getShardNumber()); // store the account in the new shard
       // network.accountToShard.remove(currentAccount);
        network.addAccount(currentAccount, receiverAccount.getShardNumber());
        System.out.println("Account :" + currentAccount + "  Now in shard N* :" + network.getAccountShard(currentAccount));
        // Log or notify about the account migration
        System.out.println("Account " + currentAccount + " migrated from Shard " +  currentAccount.getShardNumber() + " to Shard " + receiverAccount.getShardNumber());
        accountsInMigration.add(currentAccount);
        // Create a migration event
        MigrationEvent migrationEvent = new MigrationEvent(node.getSimulator().getSimulationTime(), currentAccount, currentAccount.getShardNumber(), receiverAccount.getShardNumber(),migrationThreshold,((PBFTShardedNetwork)this.network).clientCrossShardTransactions, ((PBFTShardedNetwork)this.network).clientIntraShardTransactions, ((PBFTShardedNetwork)this.network).committedTransactions,((PBFTShardedNetwork)network).MigrationCounts);
        // Put the migration event into the simulator's event queue
        node.getSimulator().putEvent(migrationEvent, 0);
    }

}