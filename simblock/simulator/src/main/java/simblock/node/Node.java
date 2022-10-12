/*
 * Copyright 2019 Distributed Systems Group
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package simblock.node;

import static simblock.settings.SimulationConfiguration.BLOCK_SIZE;
import static simblock.simulator.Main.NUM_OF_NODES;
import static simblock.simulator.Main.OUT_JSON_FILE;
import static simblock.simulator.Main.matrix;
import static simblock.simulator.Main.NEIGH_SEL;
import static simblock.simulator.Network.getBandwidth;
import static simblock.simulator.Simulator.arriveBlock;
import static simblock.simulator.Timer.getCurrentTime;
import static simblock.simulator.Timer.putTask;
import static simblock.simulator.Timer.removeTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import simblock.block.Block;
import simblock.node.consensus.AbstractConsensusAlgo;
import simblock.node.routing.AbstractRoutingTable;
import simblock.task.AbstractMessageTask;
import simblock.task.AbstractMintingTask;
import simblock.task.BlockMessageTask;
import simblock.task.InvMessageTask;
import simblock.task.RecMessageTask;

/**
 * A class representing a node in the network.
 */
public class Node {
  /**
   * Unique node ID.
   */
  private final int nodeID;

  /**
   * Region assigned to the node.
   */
  private int region_id;

  private String ip;

  private Double latitude;

  private Double longitude;

  private String location;

  private final long downloadSpeed;

  private final long uploadSpeed;

  /**
   * Mining power assigned to the node.
   */
  private final int miningPower;

  /**
   * A nodes routing table.
   */
  private AbstractRoutingTable routingTable;

  /**
   * The consensus algorithm used by the node.
   */
  private AbstractConsensusAlgo consensusAlgo;

  /**
   * The current block.
   */
  private Block block;

  /**
   * Orphaned blocks known to node.
   */
  private final Set<Block> orphans = new HashSet<>();

  /**
   * The current minting task
   */
  private AbstractMintingTask mintingTask = null;

  /**
   * In the process of sending blocks.
   */
  // TODO verify
  private boolean sendingBlock = false;

  // TODO
  private final ArrayList<AbstractMessageTask> messageQue = new ArrayList<>();
  // TODO
  private final Set<Block> downloadingBlocks = new HashSet<>();

  /**
   * Processing time of tasks expressed in milliseconds.
   */
  private final long processingTime = 2;

  public Node(
      int nodeID, int numConnection, String ip, int region_id, Double latitude, Double longitude,
      String location, int miningPower, long downloadSpeed, long uploadSpeed) {
    this.nodeID = nodeID;
    this.region_id = region_id;
    this.ip = ip;
    this.latitude = latitude;
    this.longitude = longitude;
    this.location = location;
    this.miningPower = miningPower;
    this.downloadSpeed = downloadSpeed;
    this.uploadSpeed = uploadSpeed;

    try {
      this.routingTable = (AbstractRoutingTable) Class.forName("simblock.node.routing.BitcoinCoreTable").getConstructor(
          Node.class).newInstance(this);
      this.consensusAlgo = (AbstractConsensusAlgo) Class.forName("simblock.node.consensus.ProofOfWork").getConstructor(
          Node.class).newInstance(this);
      this.setNumConnection(numConnection);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets the node id.
   *
   * @return the node id
   */
  public int getNodeID() {
    return this.nodeID;
  }

  /**
   * Gets the region ID assigned to a node.
   *
   * @return the region
   */
  public int getRegionID() {
    return this.region_id;
  }

  public void setRegionID(int region_id) {
    this.region_id = region_id;
  }


  public String getIP() {
    return this.ip;
  }

  public Double getLatitude() {
    return this.latitude;
  }

  public Double getLongitude() {
    return this.longitude;
  }

  public String getLocation() {
    return this.location;
  }

  /**
   * Gets mining power.
   *
   * @return the mining power
   */
  public long getMiningPower() {
    return this.miningPower;
  }

  /**
   * Gets the consensus algorithm.
   *
   * @return the consensus algorithm. See {@link AbstractConsensusAlgo}
   */
  @SuppressWarnings("unused")
  public AbstractConsensusAlgo getConsensusAlgo() {
    return this.consensusAlgo;
  }

  /**
   * Gets routing table.
   *
   * @return the routing table
   */
  public AbstractRoutingTable getRoutingTable() {
    return this.routingTable;
  }

  /**
   * Gets the current block.
   *
   * @return the block
   */
  public Block getBlock() {
    return this.block;
  }

  /**
   * Gets all orphans known to node.
   *
   * @return the orphans
   */
  public Set<Block> getOrphans() {
    return this.orphans;
  }

  /**
   * Gets the number of connections a node can have.
   *
   * @return the number of connection
   */
  @SuppressWarnings("unused")
  public int getNumConnection() {
    return this.routingTable.getNumConnection();
  }

  /**
   * Sets the number of connections a node can have.
   *
   * @param numConnection the n connection
   */
  public void setNumConnection(int numConnection) {
    this.routingTable.setNumConnection(numConnection);
  }

  /**
   * Gets the nodes neighbors.
   *
   * @return the neighbors
   */
  public ArrayList<Node> getNeighbors() {
    return this.routingTable.getNeighbors();
  }

  public long getUploadSpeed() {
    return this.uploadSpeed;
  }

  public long getDownloadSpeed() {
    return this.downloadSpeed;
  }

  /**
   * Adds the node as a neighbor.
   *
   * @param node the node to be added as a neighbor
   * @return the success state of the operation
   */
  @SuppressWarnings("UnusedReturnValue")
  public boolean addNeighbor(Node node) {
    return this.routingTable.addNeighbor(node);
  }

  /**
   * Removes the neighbor form the node.
   *
   * @param node the node to be removed as a neighbor
   * @return the success state of the operation
   */
  @SuppressWarnings("unused")
  public boolean removeNeighbor(Node node) {
    return this.routingTable.removeNeighbor(node);
  }

  /**
   * Initializes the routing table.
   */
  public void joinNetwork(int CON_ALG) {
      this.routingTable.initTable(CON_ALG);
  }

  public void joinNetworkBCBSN(int CON_ALG, ArrayList<Node> nodelist) {
    this.routingTable.initTableBCBSN(CON_ALG, nodelist);
}

  /**
   * Mint the genesis block.
   */
  public void genesisBlock() {
    Block genesis = this.consensusAlgo.genesisBlock();
    this.receiveBlock(genesis, null, this);
  }

  /**
   * Adds a new block to the to chain. If node was minting that task instance is
   * abandoned, and
   * the new block arrival is handled.
   *
   * @param newBlock the new block
   */
  public void addToChain(Block newBlock) {
    // If the node has been minting
    if (this.mintingTask != null) {
      removeTask(this.mintingTask);
      this.mintingTask = null;
    }
    // Update the current block
    this.block = newBlock;
    printAddBlock(newBlock);
    // Observe and handle new block arrival
    arriveBlock(newBlock, this, NUM_OF_NODES);
  }

  /**
   * Logs the provided block to the logfile.
   *
   * @param newBlock the block to be logged
   */
  private void printAddBlock(Block newBlock) {
    OUT_JSON_FILE.print("{");
    OUT_JSON_FILE.print("\"kind\":\"add-block\",");
    OUT_JSON_FILE.print("\"content\":{");
    OUT_JSON_FILE.print("\"timestamp\":" + getCurrentTime() + ",");
    OUT_JSON_FILE.print("\"node-id\":" + this.getNodeID() + ",");
    OUT_JSON_FILE.print("\"block-id\":" + newBlock.getId() + ",");
    OUT_JSON_FILE.print("\"network-route\":" + newBlock.getRoute().get(this.getNodeID()));
    OUT_JSON_FILE.print("}");
    OUT_JSON_FILE.print("},");
    OUT_JSON_FILE.flush();
  }

  /**
   * Add orphans.
   *
   * @param orphanBlock the orphan block
   * @param validBlock  the valid block
   */
  // TODO check this out later
  public void addOrphans(Block orphanBlock, Block validBlock) {
    if (orphanBlock != validBlock) {
      this.orphans.add(orphanBlock);
      this.orphans.remove(validBlock);
      if (validBlock == null || orphanBlock.getHeight() > validBlock.getHeight()) {
        this.addOrphans(orphanBlock.getParent(), validBlock);
      } else if (orphanBlock.getHeight() == validBlock.getHeight()) {
        this.addOrphans(orphanBlock.getParent(), validBlock.getParent());
      } else {
        this.addOrphans(orphanBlock, validBlock.getParent());
      }
    }
  }

  /**
   * Generates a new minting task and registers it
   */
  public void minting() {
    AbstractMintingTask task = this.consensusAlgo.minting();
    this.mintingTask = task;
    if (task != null) {
      putTask(task);
    }
  }

  /**
   * Send inv.
   *
   * @param block the block
   */
  public void sendInv(Block block) {
    for (Node to : this.routingTable.getOutbound()) {
      AbstractMessageTask task = new InvMessageTask(this, to, block);
      putTask(task);
    }
  }

  /**
   * Receive block.
   *
   * @param block the block
   */
  public void receiveBlock(Block block, Node from, Node to) {

    if (this.consensusAlgo.isReceivedBlockValid(block, this.block)) {
      if (this.block != null && !this.block.isOnSameChainAs(block)) {
        // If orphan mark orphan
        this.addOrphans(this.block, block);
      }
      // Else add to canonical chain

      ArrayList<Integer> nodes = new ArrayList<>();

      if (from != null && !block.getRoute().get(from.nodeID).isEmpty()) {
        for (int j = 0; j < block.getRoute().get(from.nodeID).size(); j++) {
          nodes.add(block.getRoute().get(from.nodeID).get(j));
        }
        block.getRoute().put(to.nodeID, nodes);
        if (!block.getRoute().get(to.nodeID).contains(from.nodeID)) {
          block.getRoute().get(to.nodeID).add(from.nodeID);
        }
      } else {
        nodes.add(to.nodeID);
        block.getRoute().put(to.nodeID, nodes);
      }

      this.addToChain(block);

      // Generates a new minting task
      this.minting();
      // Advertise received block
      this.sendInv(block);
    } else if (!this.orphans.contains(block) && !block.isOnSameChainAs(this.block)) {
      // TODO better understand - what if orphan is not valid?
      // If the block was not valid but was an unknown orphan and is not on the same
      // chain as the
      // current block
      this.addOrphans(block, this.block);
      arriveBlock(block, this, NUM_OF_NODES);
    }
  }

  /**
   * Receive message.
   *
   * @param message the message
   */
  public void receiveMessage(AbstractMessageTask message) {
    Node from = message.getFrom();
    Node to = message.getTo();

    if (message instanceof InvMessageTask) {
      Block block = ((InvMessageTask) message).getBlock();
      long time = getCurrentTime() - block.getTime();
      if (from != null && (NEIGH_SEL.equals("Y") || NEIGH_SEL.equals("y"))) {
        if (from.nodeID != to.nodeID) {
          if (matrix[from.nodeID][to.nodeID] != 0) {
            matrix[from.nodeID][to.nodeID] = (long) ((1 - 0.3) * matrix[from.nodeID][to.nodeID] + 0.3 * (time));
          } else {
            matrix[from.nodeID][to.nodeID] = time;
          }
        }
      }
      if (!this.orphans.contains(block) && !this.downloadingBlocks.contains(block)) {
        if (this.consensusAlgo.isReceivedBlockValid(block, this.block)) {
          AbstractMessageTask task = new RecMessageTask(this, from, block);
          putTask(task);
          downloadingBlocks.add(block);
        } else if (!block.isOnSameChainAs(this.block)) {
          // get new orphan block
          AbstractMessageTask task = new RecMessageTask(this, from, block);
          putTask(task);
          downloadingBlocks.add(block);
        }
      }
    }

    if (message instanceof RecMessageTask) {
      this.messageQue.add((RecMessageTask) message);
      if (!sendingBlock) {
        this.sendNextBlockMessage();
      }
    }

    if (message instanceof BlockMessageTask) {
      Block block = ((BlockMessageTask) message).getBlock();
      downloadingBlocks.remove(block);
      this.receiveBlock(block, from, this);
    }
  }
  /**
   * Send next block message.
   */
  // send a block to the sender of the next queued recMessage
  public void sendNextBlockMessage() {
    if (this.messageQue.size() > 0) {
      Node to = this.messageQue.get(0).getFrom();
      long bandwidth = getBandwidth(this, to);

      AbstractMessageTask messageTask;

      if (this.messageQue.get(0) instanceof RecMessageTask) {
        Block block = ((RecMessageTask) this.messageQue.get(0)).getBlock();
        long delay = BLOCK_SIZE * 8 / (bandwidth / 1000) + processingTime;
        messageTask = new BlockMessageTask(this, to, block, delay);
      } else {
        throw new UnsupportedOperationException();
      }

      sendingBlock = true;
      this.messageQue.remove(0);
      putTask(messageTask);
    } else {
      sendingBlock = false;
    }
  }
}
