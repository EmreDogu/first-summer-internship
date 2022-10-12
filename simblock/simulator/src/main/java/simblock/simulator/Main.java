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

package simblock.simulator;

import java.util.concurrent.ThreadLocalRandom;
import static simblock.settings.SimulationConfiguration.AVERAGE_MINING_POWER;
import static simblock.settings.SimulationConfiguration.INTERVAL;
import static simblock.settings.SimulationConfiguration.STDEV_OF_MINING_POWER;
import static simblock.simulator.Network.printRegion;
import static simblock.settings.NetworkConfiguration.DOWNLOAD_BANDWIDTH;
import static simblock.settings.NetworkConfiguration.UPLOAD_BANDWIDTH;
import static simblock.simulator.Simulator.addNode;
import static simblock.simulator.Simulator.getSimulatedNodes;
import static simblock.simulator.Simulator.printAllPropagation;
import static simblock.simulator.Simulator.setTargetInterval;
import static simblock.simulator.Timer.getCurrentTime;
import static simblock.simulator.Timer.getTask;
import static simblock.simulator.Timer.runTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

import simblock.block.Block;
import simblock.node.Node;
import simblock.task.AbstractMintingTask;
import com.google.gson.*;

/**
 * The type Main represents the entry point.
 */
public class Main {

  /**
   * The constant to be used as the simulation seed.
   */
  public static Random random = new Random(10);

  public static Integer NUM_OF_NODES;

  public static Integer NUM_OF_BLOCKS;

  public static Integer CON_ALG;

  public static Integer NODE_LIST;

  public static String NEIGH_SEL;

  public static Integer BLOCK_PROX;

  /**
   * The initial simulation time.
   */
  public static long simulationTime = 0;
  /**
   * Path to config file.
   */
  /**
   * Output path.
   */

  public static long[][] matrix;

  /**
   * The output writer.
   */
  // TODO use logger
  public static PrintWriter OUT_JSON_FILE;

  /**
   * The constant STATIC_JSON_FILE.
   */
  // TODO use logger
  public static PrintWriter STATIC_JSON_FILE;

  static {
    try {
      OUT_JSON_FILE = new PrintWriter(
          new BufferedWriter(new FileWriter(new File("simblock/simulator/src/dist/output/output.json"))));
      STATIC_JSON_FILE = new PrintWriter(
          new BufferedWriter(new FileWriter(new File("simblock/simulator/src/dist/output/static.json"))));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * The entry point.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    Scanner myObj = new Scanner(System.in);

    System.out.println("Enter number of nodes for this simulation : ");
    NUM_OF_NODES = myObj.nextInt();

    System.out.println("Enter number of blocks to be minted for this simulation: ");
    NUM_OF_BLOCKS = myObj.nextInt();

    System.out.println("Enter the way nodes to be added (1-custom, 2-random generated, 3-bitnodes): ");
    NODE_LIST = myObj.nextInt();

    System.out.println(
        "Enter the way nodes connect (1-manual, 2-randompair, 3-nearpair, 4-clusterpair, 5-halfpair, 6-nmfpair, 7-TwoContinentBCBSN, 8-BCBSN): ");
    CON_ALG = myObj.nextInt();

    System.out.println("Enter number of outbound connections for this simulation: ");
    Integer NUM_OF_CON = myObj.nextInt();

    System.out.println("Do you allow proximity neighbor selection algorithm (Y-N): ");
    NEIGH_SEL = myObj.nextLine();

    if (NEIGH_SEL.equals("y") || NEIGH_SEL.equals("Y")) {
      System.out.println("Per how many blocks do you want proximity neighbor selection algortihm to be applied: ");
      BLOCK_PROX = myObj.nextInt();
    }

    myObj.nextLine();
    myObj.close();

    final long start = System.currentTimeMillis();
    setTargetInterval(INTERVAL);

    if (NEIGH_SEL.equals("Y") || NEIGH_SEL.equals("y")) {

      matrix = new long[NUM_OF_NODES][NUM_OF_NODES];

      for (int i = 0; i < NUM_OF_NODES; i++) {
        for (int j = 0; j < NUM_OF_NODES; j++) {
          if (i == j) {
            continue;
          } else {
            matrix[i][j] = 0;
          }
        }
      }
    }

    // start json format
    OUT_JSON_FILE.print("[");
    OUT_JSON_FILE.flush();

    // Log regions
    printRegion();

    // Setup network
    if (NODE_LIST == 3 || NODE_LIST == 1) {
      constructNetworkWithAllNodes(NUM_OF_NODES, NUM_OF_CON, NODE_LIST);
    } else {
      constructNetworkRandomly(NUM_OF_NODES, NUM_OF_CON);
    }

    // Initial block height, we stop at END_BLOCK_HEIGHT
    int currentBlockHeight = 1;

    // Iterate over tasks and handle
    while (getTask() != null) {
      if (getTask() instanceof AbstractMintingTask) {
        AbstractMintingTask task = (AbstractMintingTask) getTask();
        if (task.getParent().getHeight() == currentBlockHeight) {
          currentBlockHeight++;
        }
        if (currentBlockHeight > NUM_OF_BLOCKS) {
          break;
        }
        if (NEIGH_SEL.equals("Y") || NEIGH_SEL.equals("y")) {
          if (currentBlockHeight % BLOCK_PROX == 0) {
            for (Node node : getSimulatedNodes()) {
              node.getRoutingTable().reconnectAll(matrix, NUM_OF_NODES);
            }
          }
        }
        // Log every 100 blocks and at the second block
        // TODO use constants here
        if (currentBlockHeight % 100 == 0 || currentBlockHeight == 2) {
          writeGraph(currentBlockHeight);
        }
      }
      // Execute task
      runTask();
    }

    // Print propagation information about all blocks
    printAllPropagation(NUM_OF_NODES);

    // TODO logger
    System.out.println();

    Set<Block> blocks = new HashSet<>();

    // Get the latest block from the first simulated node
    Block block = getSimulatedNodes().get(0).getBlock();

    // Update the list of known blocks by adding the parents of the aforementioned
    // block
    while (block.getParent() != null) {
      blocks.add(block);
      block = block.getParent();
    }

    ArrayList<Block> blockList = new ArrayList<>(blocks);

    // Sort the blocks first by time, then by hash code
    blockList.sort((a, b) -> {
      int order = Long.signum(a.getTime() - b.getTime());
      if (order != 0) {
        return order;
      }
      order = System.identityHashCode(a) - System.identityHashCode(b);
      return order;
    });
    /*
     * Log in format:
     * fork_information, block height, block ID
     * fork_information: One of "OnChain" and "Orphan". "OnChain" denote block is on
     * Main chain.
     * "Orphan" denote block is an orphan block.
     */
    // TODO move to method and use logger
    try {
      FileWriter fw = new FileWriter(new File("simblock/simulator/src/dist/output/blockList.txt"), false);
      PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

      for (Block b : blockList) {
        pw.println("OnChain : " + b.getHeight() + " : " + b);
      }
      pw.close();

    } catch (IOException ex) {
      ex.printStackTrace();
    }

    OUT_JSON_FILE.print("{");
    OUT_JSON_FILE.print("\"kind\":\"simulation-end\",");
    OUT_JSON_FILE.print("\"content\":{");
    OUT_JSON_FILE.print("\"timestamp\":" + getCurrentTime());
    OUT_JSON_FILE.print("}");
    OUT_JSON_FILE.print("}");
    // end json format
    OUT_JSON_FILE.print("]");
    OUT_JSON_FILE.close();

    long end = System.currentTimeMillis();
    simulationTime += end - start;
    // Log simulation time in milliseconds
    System.out.println(simulationTime);

  }

  public static void constructNetworkRandomly(int numNodes, int numCon) {
    for (int id = 0; id < numNodes; id++) {
      int region_id = ThreadLocalRandom.current().nextInt(0, 7);
      Double latitude = ThreadLocalRandom.current().nextDouble(-90, 91);
      Double longitude = ThreadLocalRandom.current().nextDouble(-180, 1811);
      int miningPower = genMiningPower();
      long downloadSpeed = DOWNLOAD_BANDWIDTH[region_id];
      long uploadSpeed = UPLOAD_BANDWIDTH[region_id];
      Node node = new Node(id, numCon, "",
          region_id,
          latitude,
          longitude,
          "", miningPower, downloadSpeed, uploadSpeed);
      addNode(node);

      OUT_JSON_FILE.print("{");
      OUT_JSON_FILE.print("\"kind\":\"add-node\",");
      OUT_JSON_FILE.print("\"content\":{");
      OUT_JSON_FILE.print("\"timestamp\":0,");
      OUT_JSON_FILE.print("\"node-id\":" + id + ",");
      OUT_JSON_FILE.print("\"region-id\":" + node.getRegionID());
      OUT_JSON_FILE.print("}");
      OUT_JSON_FILE.print("},");
      OUT_JSON_FILE.flush();
    }

    // Link newly generated nodes
    if (CON_ALG < 7 & CON_ALG > 0) {
      for (Node node : getSimulatedNodes()) {
        node.joinNetwork(CON_ALG);
      }
    } else if (CON_ALG == 7 || CON_ALG == 8) {
      getSimulatedNodes().get(0).joinNetworkBCBSN(CON_ALG, getSimulatedNodes());
    }

    // Designates a random node (nodes in list are randomized) to mint the genesis
    // block
    getSimulatedNodes().get(0).genesisBlock();
  }

  /**
   * Generates a random mining power expressed as Hash Rate, and is the number of
   * mining (hash
   * calculation) executed per millisecond.
   *
   * @return the number of hash calculations executed per millisecond.
   */
  public static int genMiningPower() {
    double r = random.nextGaussian();

    return Math.max((int) (r * STDEV_OF_MINING_POWER + AVERAGE_MINING_POWER), 1);
  }

  /**
   * Construct network with the provided number of nodes.
   *
   * @param numNodes the num nodes
   */
  public static void constructNetworkWithAllNodes(int numNodes, int numCon, int nodeList) {
    try {
      BufferedReader br = new BufferedReader(new FileReader("simblock/simulator/src/dist/input/nodes.json"));
      JsonParser parser = new JsonParser();
      JsonObject jobject = parser.parse(br).getAsJsonObject().getAsJsonObject("nodes");
      Set<Entry<String, JsonElement>> entries = jobject.entrySet();
      int id = 0;
      for (Map.Entry<String, JsonElement> entry : entries) {
        if (nodeList == 3) {
          if (!(jobject.getAsJsonArray(entry.getKey()).get(10).toString().replaceAll("\"", "").equals("null"))) {
            Integer region_id = 0;
            if (jobject.getAsJsonArray(entry.getKey()).get(10).toString().contains("America")) {
              region_id = 0;
            } else if (jobject.getAsJsonArray(entry.getKey()).get(10).toString().contains("Europe")) {
              region_id = 1;
            } else if (jobject.getAsJsonArray(entry.getKey()).get(10).toString().contains("Asia")) {
              region_id = 2;
            } else if (jobject.getAsJsonArray(entry.getKey()).get(10).toString().contains("Australia")) {
              region_id = 3;
            } else if (jobject.getAsJsonArray(entry.getKey()).get(10).toString().contains("Africa")) {
              region_id = 4;
            } else if (jobject.getAsJsonArray(entry.getKey()).get(10).toString().contains("Atlantic")) {
              region_id = 5;
            } else if (jobject.getAsJsonArray(entry.getKey()).get(10).toString().contains("Pacific")) {
              region_id = 6;
            }

            int miningPower = 0;
            if (jobject.getAsJsonArray(entry.getKey()).size() <= 13) {
              miningPower = genMiningPower();
            } else {
              miningPower = Integer
                  .parseInt(jobject.getAsJsonArray(entry.getKey()).get(13).toString().replaceAll("\"", ""));
            }

            long downloadSpeed = 0;
            if (jobject.getAsJsonArray(entry.getKey()).size() <= 13) {
              downloadSpeed = DOWNLOAD_BANDWIDTH[region_id];
            } else {
              downloadSpeed = Integer
                  .parseInt(jobject.getAsJsonArray(entry.getKey()).get(14).toString().replaceAll("\"", ""));
            }

            long uploadSpeed = 0;
            if (jobject.getAsJsonArray(entry.getKey()).size() <= 13) {
              uploadSpeed = UPLOAD_BANDWIDTH[region_id];
            } else {
              uploadSpeed = Integer
                  .parseInt(jobject.getAsJsonArray(entry.getKey()).get(15).toString().replaceAll("\"", ""));
            }

            String[] location = jobject.getAsJsonArray(entry.getKey()).get(10).toString().replaceAll("\"", "")
                .split("/");

            Node node = new Node(id, numCon, entry.getKey(), region_id,
                Double.parseDouble(jobject.getAsJsonArray(entry.getKey()).get(8).toString()),
                Double.parseDouble(jobject.getAsJsonArray(entry.getKey()).get(9).toString()),
                location[location.length - 1], miningPower, downloadSpeed, uploadSpeed);
            addNode(node);

            OUT_JSON_FILE.print("{");
            OUT_JSON_FILE.print("\"kind\":\"add-node\",");
            OUT_JSON_FILE.print("\"content\":{");
            OUT_JSON_FILE.print("\"timestamp\":0,");
            OUT_JSON_FILE.print("\"node-id\":" + id + ",");
            OUT_JSON_FILE.print("\"region-id\":" + node.getRegionID());
            OUT_JSON_FILE.print("}");
            OUT_JSON_FILE.print("},");
            OUT_JSON_FILE.flush();

            id++;
          }
        } else if (nodeList == 1) {
          int miningPower = 0;
          if (jobject.getAsJsonArray(entry.getKey()).get(4).equals(null)) {
            miningPower = genMiningPower();
          } else {
            miningPower = Integer
                .parseInt(jobject.getAsJsonArray(entry.getKey()).get(4).toString());
          }

          long downloadSpeed = 0;
          if (jobject.getAsJsonArray(entry.getKey()).get(5).equals(null)) {
            downloadSpeed = DOWNLOAD_BANDWIDTH[Integer
                .parseInt(jobject.getAsJsonArray(entry.getKey()).get(0).toString())];
          } else {
            downloadSpeed = Integer
                .parseInt(jobject.getAsJsonArray(entry.getKey()).get(5).toString());
          }

          long uploadSpeed = 0;
          if (jobject.getAsJsonArray(entry.getKey()).get(6).equals(null)) {
            uploadSpeed = UPLOAD_BANDWIDTH[Integer
                .parseInt(jobject.getAsJsonArray(entry.getKey()).get(0).toString())];
          } else {
            uploadSpeed = Integer
                .parseInt(jobject.getAsJsonArray(entry.getKey()).get(6).toString());
          }

          Node node = new Node(id, numCon, entry.getKey(),
              Integer.parseInt(jobject.getAsJsonArray(entry.getKey()).get(0).toString()),
              Double.parseDouble(jobject.getAsJsonArray(entry.getKey()).get(1).toString()),
              Double.parseDouble(jobject.getAsJsonArray(entry.getKey()).get(2).toString()),
              jobject.getAsJsonArray(entry.getKey()).get(3).toString(), miningPower, downloadSpeed, uploadSpeed);
          addNode(node);

          OUT_JSON_FILE.print("{");
          OUT_JSON_FILE.print("\"kind\":\"add-node\",");
          OUT_JSON_FILE.print("\"content\":{");
          OUT_JSON_FILE.print("\"timestamp\":0,");
          OUT_JSON_FILE.print("\"node-id\":" + id + ",");
          OUT_JSON_FILE.print("\"region-id\":" + node.getRegionID());
          OUT_JSON_FILE.print("}");
          OUT_JSON_FILE.print("},");
          OUT_JSON_FILE.flush();

          id++;
        }
        if (id == numNodes) {
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Link newly generated nodes
    if (CON_ALG < 7 & CON_ALG > 0) {
      for (Node node : getSimulatedNodes()) {
        node.joinNetwork(CON_ALG);
      }
    } else if (CON_ALG == 7 || CON_ALG == 8) {
      getSimulatedNodes().get(0).joinNetworkBCBSN(CON_ALG, getSimulatedNodes());
    }

    // Designates a random node (nodes in list are randomized) to mint the genesis
    // block
    getSimulatedNodes().get(0).genesisBlock();
  }

  /**
   * Network information when block height is <em>blockHeight</em>, in format:
   *
   * <p>
   * <em>nodeID_1</em>, <em>nodeID_2</em>
   *
   * <p>
   * meaning there is a connection from nodeID_1 to right nodeID_1.
   *
   * @param blockHeight the index of the graph and the current block height
   */
  // TODO use logger
  public static void writeGraph(int blockHeight) {
    try {
      FileWriter fw = new FileWriter(
          new File("simblock/simulator/src/dist/output/graph/" + blockHeight + ".txt"), false);
      PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

      for (int index = 1; index <= getSimulatedNodes().size(); index++) {
        Node node = getSimulatedNodes().get(index - 1);
        for (int i = 0; i < node.getNeighbors().size(); i++) {
          Node neighbor = node.getNeighbors().get(i);
          pw.println(node.getNodeID() + " " + neighbor.getNodeID());
        }
      }
      pw.close();

    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}