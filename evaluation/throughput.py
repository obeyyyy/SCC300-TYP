import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

def process_csv(df, nameOfFile):
    # Time, Tx Hash, Time Tx Created
    # Latency = Time - Time Tx Created
    print("this is for file: ", nameOfFile)
    df['Latency'] = df['Time'] - df['TxCreationTime']
    average_latency = df['Latency'].mean()
    print('Average Latency: ', round(average_latency, 2) ,'seconds')

    # print the length of the dataframe
    print('Committed Txs: ', len(df))

    # print the transactions per second
    print('Committed Txs per second: ', round(len(df)/df['Time'].iloc[-1], 2))

    crossShardOrNot = df["CrossShard"]
    # get the amount that are True
    crossShard = crossShardOrNot[crossShardOrNot == True]
    # get the amount that are False
    intraShard = crossShardOrNot[crossShardOrNot == False]
    print("Cross Shard Txs: ", len(crossShard))
    print("Intra Shard Txs: ", len(intraShard))

    # print the time of the last tx
    print('Last Tx Time: ', df['Time'].iloc[-1])

    # create a cumulative committed transactions
    df['CumulativeCommittedTxs'] = df.index + 1

    # create cumulative committed cross shard transactions
    df['CumulativeCommittedCrossShardTxs'] = df['CrossShard'].cumsum()
    # create cumulative committed intra shard transactions
    df['CumulativeCommittedIntraShardTxs'] = df['CumulativeCommittedTxs'] - df['CumulativeCommittedCrossShardTxs']
    # print(df)
    print("")
    return df, average_latency


df_1_shard = pd.read_csv('output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-2s10n800c.csv')
# client led
df_2_shard_clientled = pd.read_csv('output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-2s10n800c.csv')
df_4_shard_clientled = pd.read_csv('output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-4s10n800c.csv')
df_6_shard_clientled = pd.read_csv('output/tenNodesSimulations/clientled/exponent1.2/seed1/Clientled-CommittedLogger-5s10n800c.csv')


# process the data
df_2_shard_clientled, df_2_shard_clientled_latency = process_csv(df_2_shard_clientled, '2_shard_clientled')
df_4_shard_clientled, df_4_shard_clientled_latency = process_csv(df_4_shard_clientled, '4_shard_clientled')
df_6_shard_clientled, df_6_shard_clientled_latency = process_csv(df_6_shard_clientled, '5_shard_clientled')

fig, ax = plt.subplots()

ax.plot(df_2_shard_clientled['Time'], df_2_shard_clientled['CumulativeCommittedTxs'], label='2 Shard Clientled')
ax.plot(df_4_shard_clientled['Time'], df_4_shard_clientled['CumulativeCommittedTxs'], label='4 Shard Clientled')
ax.plot(df_6_shard_clientled['Time'], df_6_shard_clientled['CumulativeCommittedTxs'], label='5 Shard Clientled')



ax.set_xlabel('Time (s)', size=14)
ax.set_ylabel('Cumulative Committed Transactions', size=14)

ax.set_title('Cumulative Committed Transactions for Different Configurations')


ax.legend()


# plt.show()


# fig, ax = plt.subplots()

# ax.bar('2 shards', df_2_shard_clientled_latency, label='2 Shards Clientled')
# ax.bar('4 shards', df_4_shard_clientled_latency, label='4 Shards Clientled')
# ax.bar('6 shards', df_6_shard_clientled_latency, label='6 Shards Clientled')
# ax.bar('8 shards', df_8_shard_clientled_latency, label='8 Shards Clientled')
# ax.bar('16 shards', df_16_shard_clientled_latency, label='16 Shards Clientled')
# ax.bar('32 shards', df_32_shard_clientled_latency, label='32 Shards Clientled')
# ax.bar('2 shards', df_2_shard_shardled_latency, label='2 Shards Shardled')
# ax.bar('4 shards', df_4_shard_shardled_latency, label='4 Shards Shardled')
# ax.bar('6 shards', df_6_shard_shardled_latency, label='6 Shards Shardled')
# ax.bar('8 shards', df_8_shard_shardled_latency, label='8 Shards Shardled')
# ax.bar('16 shards', df_16_shard_shardled_latency, label='16 Shards Shardled')
# ax.bar('32 shards', df_32_shard_shardled_latency, label='32 Shards Shardled')


# ax.set_xlabel('Configuration')
# ax.set_ylabel('Latency')

# ax.set_title('Latency for Different Configurations')

# ax.legend()

# plt.show()

# Prepare data for plotting
shard_counts = np.array([2, 4, 6])
clientled_latencies = np.array([df_2_shard_clientled_latency, df_4_shard_clientled_latency, df_6_shard_clientled_latency])

# Set up plot
fig, ax = plt.subplots()
width = 0.35
x = np.arange(len(shard_counts))

# Plot bars side by side
ax.bar(x - width/2, clientled_latencies, width, label='Client-led')


# Set labels and title
ax.set_xticks(x)
ax.set_xticklabels(shard_counts)
ax.set_xlabel('Shard Count')
ax.set_ylabel('Latency (s)')
ax.set_title('Latency for Different Configurations')

# Add legend
ax.legend()

# Show plot
plt.show()


data = {'Configuration': ['2 Shards Clientled', '4 Shards Clientled', '6 Shards Clientled',
                          '8 Shards Clientled', '16 Shards Clientled', '32 Shards Clientled',
                          '2 Shards Shardled', '4 Shards Shardled', '6 Shards Shardled',
                          '8 Shards Shardled', '16 Shards Shardled', '32 Shards Shardled'],
        'Latency': [df_2_shard_clientled_latency, df_4_shard_clientled_latency, df_6_shard_clientled_latency]}


summary_table = pd.DataFrame(data)

fig, ax = plt.subplots()

ax.axis('off')

table = ax.table(cellText=summary_table.values, colLabels=summary_table.columns, loc='center')

ax.set_title('Summary Table for Latency of Different Configurations')

plt.show()

def calculate_total_transactions_and_tps(df):
    total_transactions = len(df)
    tps = round(total_transactions / df['Time'].iloc[-1], 2)
    return total_transactions, tps

total_transactions_and_tps = [calculate_total_transactions_and_tps(df) for df in [df_2_shard_clientled,
                                                                                  df_4_shard_clientled,
                                                                                  df_6_shard_clientled]]


data['Total Transactions'] = [vals[0] for vals in total_transactions_and_tps]
data['Transactions per Second'] = [vals[1] for vals in total_transactions_and_tps]


summary_table = pd.DataFrame(data)

fig, ax = plt.subplots()

ax.axis('off')

table = ax.table(cellText=summary_table.values, colLabels=summary_table.columns, loc='center')
table.auto_set_font_size(False)
table.set_fontsize(16)
ax.set_title('Summary Table for Latency, Total Transactions, and Transactions per Second of Different Configurations')
plt.show()