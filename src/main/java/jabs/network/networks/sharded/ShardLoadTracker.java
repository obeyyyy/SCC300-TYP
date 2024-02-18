package jabs.network.networks.sharded;
import java.util.HashMap;
import java.util.Map;

    // this class is used to keep track of the loads of the shards to determine which shard
    // has the least load therefore migrate new accounts or exisiting accounts to it
public class ShardLoadTracker {
    private Map<Integer, Integer> shardLoads; // Map of shard ID to load

    public ShardLoadTracker() {
        this.shardLoads = new HashMap<>();
    }

    // Method to add a shard with initial load
    public void addShard(int shardId, int initialLoad) {
        shardLoads.put(shardId, initialLoad);
    }

    // Method to update the load of a shard
    public void updateLoad(int shardId, int newLoad) {
        shardLoads.put(shardId, newLoad);
    }

    // Method to get the load of a shard
    public int getLoad(int shardId) {
        return shardLoads.getOrDefault(shardId, 0);
    }

    
    // Method to find the least loaded shard
    public int getLeastLoadedShard() {
        int leastLoad = Integer.MAX_VALUE;
        int leastLoadedShard = -1;

        for (Map.Entry<Integer, Integer> entry : shardLoads.entrySet()) {
            int load = entry.getValue();
            if (load < leastLoad) {
                leastLoad = load;
                leastLoadedShard = entry.getKey();
            }
        }
        return leastLoadedShard; // main shard
    }

    public  Map<Integer, Integer> getShardLoads()
    {
        return shardLoads;
    }

}