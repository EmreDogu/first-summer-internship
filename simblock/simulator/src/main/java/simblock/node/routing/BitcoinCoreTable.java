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

package simblock.node.routing;

import static simblock.simulator.Main.OUT_JSON_FILE;
import static simblock.simulator.Simulator.getSimulatedNodes;
import static simblock.simulator.Timer.getCurrentTime;

import java.io.*;
import java.util.*;
import simblock.node.Node;

/**
 * The implementation of the {@link AbstractRoutingTable} representing the
 * Bitcoin core routing
 * table.
 */
@SuppressWarnings("unused")
public class BitcoinCoreTable extends AbstractRoutingTable {

  /**
   * The list of outbound connections.
   */
  private final ArrayList<Node> outbound = new ArrayList<>();

  /**
   * The list of inbound connections.
   */
  private final ArrayList<Node> inbound = new ArrayList<>();

  /**
   * Instantiates a new Bitcoin core table.
   *
   * @param selfNode the self node
   */
  public BitcoinCoreTable(Node selfNode) {
    super(selfNode);
  }

  /**
   * Gets all known outbound and inbound nodes.
   *
   * @return a list of known neighbors
   */
  public ArrayList<Node> getNeighbors() {
    ArrayList<Node> neighbors = new ArrayList<>();
    neighbors.addAll(outbound);
    neighbors.addAll(inbound);
    return neighbors;
  }

  public ArrayList<Node> getInbound() {
    return this.inbound;
  }

  public ArrayList<Node> getOutbound() {
    return this.outbound;
  }

  public void removeInbounds(Node node) {
    for (Node a : node.getRoutingTable().getInbound()) {
      a.getRoutingTable().getOutbound().remove(node);
    }
    node.getRoutingTable().getInbound().clear();
  }

  /**
   * Initializes a new BitcoinCore routing table. From a pool of
   * all available nodes, choose candidates at random and
   * fill the table using the allowed outbound connections
   * amount.
   */
  // TODO this should be done using the bootstrap node
  public void initTable(int algo) {
    if (algo == 1) {
      try {
        Scanner scanner = new Scanner(new File("simblock/simulator/src/dist/input/nodes.txt"));
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          if (line.contains("selected")
              && (this.getSelfNode().getNodeID() == Integer.valueOf(line.substring(line.indexOf("Node") + 5,
                  line.indexOf("has") - 1)))) {
            this.addNeighbor(getSimulatedNodes().get(Integer.valueOf(line.substring(line.indexOf("selected") + 9,
                line.lastIndexOf("as") - 1))));
          }
        }
        scanner.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } else if (algo == 2) {
      List<String> holderlist = new ArrayList<String>();
      List<Node> nodelist = getSimulatedNodes();

      while (this.getSelfNode().getRoutingTable().getOutbound().size() < this.getSelfNode().getNumConnection()) {

        if (holderlist.size() == nodelist.size() - 1) {
          System.out
              .println("Can't complete node id: " + this.getSelfNode().getNodeID()
                  + "'s outgoing connection to " + this.getSelfNode().getNumConnection() + " Final count was: "
                  + this.getSelfNode().getRoutingTable().getOutbound().size() + ". Moving on to next node\n");
          holderlist.clear();
          break;
        }

        Random random = new Random();
        int randomValue;

        do {
          randomValue = random.nextInt(nodelist.size());
        } while (nodelist.get(randomValue) == this.getSelfNode()
            & !holderlist.contains(nodelist.get(randomValue).getIP()));

        if (calculateDistance(this.getSelfNode(), nodelist.get(randomValue)) > 0) {
          this.getSelfNode().addNeighbor(nodelist.get(randomValue));
          holderlist.add(nodelist.get(randomValue).getIP());

          if (this.getSelfNode().getRoutingTable().getOutbound().size() == this.getSelfNode().getNumConnection()) {
            holderlist.clear();
            break;
          }
        } else {
          holderlist.add(nodelist.get(randomValue).getIP());
        }
      }
    } else if (algo == 3) {
      List<String> holderlist = new ArrayList<String>();
      List<Node> nodelist = getSimulatedNodes();

      while (this.getSelfNode().getRoutingTable().getOutbound().size() < this.getSelfNode().getNumConnection()) {
        if (holderlist.size() == nodelist.size() - 1) {
          System.out
              .println("Can't complete node id: " + this.getSelfNode().getNodeID()
                  + "'s outgoing connection to " + this.getSelfNode().getNumConnection() + " Final count was: "
                  + this.getSelfNode().getRoutingTable().getOutbound().size() + ". Moving on to next node\n");
          holderlist.clear();
          break;
        }

        Random random = new Random();
        int randomValue;

        do {
          randomValue = random.nextInt(nodelist.size());
        } while (nodelist.get(randomValue) == this.getSelfNode()
            & !holderlist.contains(nodelist.get(randomValue).getIP()));

        if (calculateDistance(this.getSelfNode(), nodelist.get(randomValue)) <= 2500
            & calculateDistance(this.getSelfNode(), nodelist.get(randomValue)) > 0) {

          this.getSelfNode().getRoutingTable().addNeighbor(nodelist.get(randomValue));
          holderlist.add(nodelist.get(randomValue).getIP());

          if (this.getSelfNode().getRoutingTable().getOutbound().size() == this.getSelfNode().getNumConnection()) {
            holderlist.clear();
            break;
          }
        } else {
          holderlist.add(nodelist.get(randomValue).getIP());
        }
      }
    } else if (algo == 4) {
      List<String> holderlist = new ArrayList<String>();
      List<Node> nodelist = getSimulatedNodes();
      int x = 0;
      int z = 0;

      while (this.getSelfNode().getRoutingTable().getOutbound().size() < this.getSelfNode().getNumConnection()) {

        if (holderlist.size() == nodelist.size() - 1) {
          System.out
              .println("Can't complete node id: " + this.getSelfNode().getNodeID()
                  + "'s outgoing connection to " + this.getSelfNode().getNumConnection() + " Final count was: "
                  + this.getSelfNode().getRoutingTable().getOutbound().size() + ". Moving on to next node\n");
          holderlist.clear();
          break;
        }

        Random random = new Random();
        int randomValue;

        do {
          randomValue = random.nextInt(nodelist.size());
        } while (nodelist.get(randomValue) == this.getSelfNode()
            & !holderlist.contains(nodelist.get(randomValue).getIP()));

        if (x < 7 & calculateDistance(this.getSelfNode(), nodelist.get(randomValue)) <= 5500
            & calculateDistance(this.getSelfNode(), nodelist.get(randomValue)) > 0) {

          this.getSelfNode().getRoutingTable().addNeighbor(nodelist.get(randomValue));
          holderlist.add(nodelist.get(randomValue).getIP());
          x++;

          if (this.getSelfNode().getRoutingTable().getOutbound().size() == this.getSelfNode().getNumConnection()) {
            x = 0;
            z = 0;
            holderlist.clear();
            break;
          }
        } else if (z < 1 & calculateDistance(this.getSelfNode(), nodelist.get(randomValue)) > 5500) {

          this.getSelfNode().getRoutingTable().addNeighbor(nodelist.get(randomValue));
          holderlist.add(nodelist.get(randomValue).getIP());
          z++;

          if (this.getSelfNode().getRoutingTable().getOutbound().size() == this.getSelfNode().getNumConnection()) {
            x = 0;
            z = 0;
            holderlist.clear();
            break;
          }
        } else {
          holderlist.add(nodelist.get(randomValue).getIP());
        }
      }
    } else if (algo == 5) {
      List<String> holderlist = new ArrayList<String>();
      List<Node> nodelist = getSimulatedNodes();
      int x = 0;
      int z = 0;

      while (this.getSelfNode().getRoutingTable().getOutbound().size() < this.getSelfNode().getNumConnection()) {

        if (holderlist.size() == nodelist.size() - 1) {
          System.out
              .println("Can't complete node id: " + this.getSelfNode().getNodeID()
                  + "'s outgoing connection to " + this.getSelfNode().getNumConnection() + " Final count was: "
                  + this.getSelfNode().getRoutingTable().getOutbound().size() + ". Moving on to next node\n");
          holderlist.clear();
          break;
        }

        Random random = new Random();
        int randomValue;

        do {
          randomValue = random.nextInt(nodelist.size());
        } while (nodelist.get(randomValue) == this.getSelfNode()
            & !holderlist.contains(nodelist.get(randomValue).getIP()));

        if (x < 4 & calculateDistance(this.getSelfNode(), nodelist.get(randomValue)) <= 5500
            & calculateDistance(this.getSelfNode(), nodelist.get(randomValue)) > 0) {

          this.getSelfNode().getRoutingTable().addNeighbor(nodelist.get(randomValue));
          holderlist.add(nodelist.get(randomValue).getIP());
          x++;

          if (this.getSelfNode().getRoutingTable().getOutbound().size() == this.getSelfNode().getNumConnection()) {
            x = 0;
            z = 0;
            holderlist.clear();
            break;
          }
        } else if (z < 4 & calculateDistance(this.getSelfNode(), nodelist.get(randomValue)) > 5500) {

          this.getSelfNode().getRoutingTable().addNeighbor(nodelist.get(randomValue));
          holderlist.add(nodelist.get(randomValue).getIP());
          z++;

          if (this.getSelfNode().getRoutingTable().getOutbound().size() == this.getSelfNode().getNumConnection()) {
            x = 0;
            z = 0;
            holderlist.clear();
            break;
          }
        } else {
          holderlist.add(nodelist.get(randomValue).getIP());
        }
      }
    } else if (algo == 6) {
      int x = 0;
      int y = 0;
      int z = 0;
      List<String> holderlist = new ArrayList<String>();
      List<Node> nodelist = getSimulatedNodes();

      while (this.getSelfNode().getRoutingTable().getOutbound().size() < this.getSelfNode().getNumConnection()) {

        if (holderlist.size() == nodelist.size() - 1) {
          System.out
              .println("Can't complete node id: " + this.getSelfNode().getNodeID()
                  + "'s outgoing connection to " + this.getSelfNode().getNumConnection() + " Final count was: "
                  + this.getSelfNode().getRoutingTable().getOutbound().size() + ". Moving on to next node\n");
          holderlist.clear();
          break;
        }

        Random random = new Random();
        int randomValue;

        do {
          randomValue = random.nextInt(nodelist.size());
        } while (nodelist.get(randomValue) == this.getSelfNode()
            & !holderlist.contains(nodelist.get(randomValue).getIP()));

        if (x < 3 & calculateDistance(this.getSelfNode(), nodelist.get(randomValue)) <= 2500
            & calculateDistance(this.getSelfNode(), nodelist.get(randomValue)) > 0) {

          this.getSelfNode().getRoutingTable().addNeighbor(nodelist.get(randomValue));
          holderlist.add(nodelist.get(randomValue).getIP());
          x++;

          if (this.getSelfNode().getRoutingTable().getOutbound().size() == this.getSelfNode().getNumConnection()) {
            x = 0;
            y = 0;
            z = 0;
            holderlist.clear();
            break;
          }
        } else if (y < 3 & calculateDistance(this.getSelfNode(), nodelist.get(randomValue)) > 2500
            & calculateDistance(this.getSelfNode(), nodelist.get(randomValue)) <= 5500) {

          this.getSelfNode().getRoutingTable().addNeighbor(nodelist.get(randomValue));
          holderlist.add(nodelist.get(randomValue).getIP());
          y++;

          if (this.getSelfNode().getRoutingTable().getOutbound().size() == this.getSelfNode().getNumConnection()) {
            x = 0;
            y = 0;
            z = 0;
            holderlist.clear();
            break;
          }
        } else if (z < 2 & calculateDistance(this.getSelfNode(), nodelist.get(randomValue)) > 5500) {
          this.getSelfNode().getRoutingTable().addInbound(nodelist.get(randomValue));
          holderlist.add(nodelist.get(randomValue).getIP());
          z++;

          if (this.getSelfNode().getRoutingTable().getOutbound().size() == this.getSelfNode().getNumConnection()) {
            x = 0;
            y = 0;
            z = 0;
            holderlist.clear();
            break;
          }
        } else {
          holderlist.add(nodelist.get(randomValue).getIP());
        }
      }
    }
  }

  public void initTableBCBSN(int algo, ArrayList<Node> nodelist) {
    if (algo == 7) {
      Boolean flag1 = true;
      Boolean flag2 = true;
      List<Node> supernodelist = new ArrayList<Node>();
      List<String> holderlist = new ArrayList<String>();

      for (int i = 0; i < nodelist.size(); i++) {
        if (nodelist.get(i).getRegionID() == 0 & flag1) {
          flag1 = false;
          supernodelist.add(nodelist.get(i));
          nodelist.remove(nodelist.get(i));
        }
        if (nodelist.get(i).getRegionID() == 1 & flag2) {
          flag2 = false;
          supernodelist.add(nodelist.get(i));
          nodelist.remove(nodelist.get(i));
        }
        if (supernodelist.size() == 2) {
          supernodelist.get(0).addNeighbor(supernodelist.get(1));
          supernodelist.get(1).addNeighbor(supernodelist.get(0));
          break;
        }
      }

      for (int j = 0; j < nodelist.size(); j++) {
        if (calculateDistance(supernodelist.get(0), nodelist.get(j)) <= calculateDistance(supernodelist.get(1),
            nodelist.get(j))) {
          nodelist.get(j).setRegionID(0);
          nodelist.get(j).addNeighbor(supernodelist.get(0));
        } else {
          nodelist.get(j).setRegionID(1);
          nodelist.get(j).addNeighbor(supernodelist.get(1));
        }
      }

      int i = 0;

      while (i < nodelist.size()-1) {
        Random random = new Random();
        int randomValue;

        while (nodelist.get(i).getRoutingTable().getOutbound().size() < nodelist.get(i).getNumConnection()) {
          do {
            randomValue = random.nextInt(nodelist.size());
          } while (nodelist.get(randomValue) == nodelist.get(i)
              & !holderlist.contains(nodelist.get(randomValue).getIP()));

              
        if (holderlist.size() == nodelist.size() - 1) {
          System.out
              .println("Can't complete node id: " + nodelist.get(i).getNodeID()
                  + "'s outgoing connection to " + nodelist.get(i).getNumConnection() + " Final count was: "
                  + nodelist.get(i).getRoutingTable().getOutbound().size() + ". Moving on to next node\n");
          i++;
          holderlist.clear();
          if (i == nodelist.size()) {
            continue;
          }
        }

          if (calculateDistance(nodelist.get(i), nodelist.get(randomValue)) > 0
              & nodelist.get(i).getRegionID() == nodelist.get(randomValue).getRegionID()) {
            nodelist.get(i).getRoutingTable().addNeighbor(nodelist.get(randomValue));
            holderlist.add(nodelist.get(randomValue).getIP());

            if (nodelist.get(i).getRoutingTable().getOutbound().size() == nodelist.get(i).getNumConnection()) {
              i++;
              holderlist.clear();
              break;
            }
          } else {
            holderlist.add(nodelist.get(randomValue).getIP());
          }
        }
      }
    } else if (algo == 8) {
      Boolean flag1 = true;
      Boolean flag2 = true;
      Boolean flag3 = true;
      Boolean flag4 = true;
      Boolean flag5 = true;
      Boolean flag6 = true;
      Boolean flag7 = true;
      Boolean flag8 = true;
      List<Node> supernodelist = new ArrayList<Node>();
      List<String> holderlist = new ArrayList<String>();

      for (int i = 0; i < nodelist.size(); i++) {
        if (nodelist.get(i).getRegionID() == 0 & flag1) {
          flag1 = false;
          supernodelist.add(nodelist.get(i));
          nodelist.remove(nodelist.get(i));
        }
        if (nodelist.get(i).getRegionID() == 1 & flag2) {
          flag2 = false;
          supernodelist.add(nodelist.get(i));
          nodelist.remove(nodelist.get(i));
        }
        if (nodelist.get(i).getRegionID() == 2 & flag3) {
          flag3 = false;
          supernodelist.add(nodelist.get(i));
          nodelist.remove(nodelist.get(i));
        }
        if (nodelist.get(i).getRegionID() == 3 & flag4) {
          flag4 = false;
          supernodelist.add(nodelist.get(i));
          nodelist.remove(nodelist.get(i));
        }
        if (nodelist.get(i).getRegionID() == 4 & flag5) {
          flag5 = false;
          supernodelist.add(nodelist.get(i));
          nodelist.remove(nodelist.get(i));
        }
        if (nodelist.get(i).getRegionID() == 5 & flag6) {
          flag6 = false;
          supernodelist.add(nodelist.get(i));
          nodelist.remove(nodelist.get(i));
        }
        if (nodelist.get(i).getRegionID() == 6 & flag7) {
          flag7 = false;
          supernodelist.add(nodelist.get(i));
          nodelist.remove(nodelist.get(i));
        }
      }

      for (int j = 0; j < supernodelist.size(); j++) {
        for (int k = 0; k < supernodelist.size(); k++) {
          if (supernodelist.get(j).getRoutingTable().getOutbound().contains(supernodelist.get(k))
              & supernodelist.get(j).getRoutingTable().getInbound().contains(supernodelist.get(k))) {
            continue;
          } else {
            supernodelist.get(j).getRoutingTable().addNeighbor(supernodelist.get(k));
            supernodelist.get(k).getRoutingTable().addNeighbor(supernodelist.get(j));
          }
        }
      }

      for (int j = 0; j < nodelist.size(); j++) {
        HashMap<Integer, Node> distance = new HashMap<>();
        for (Node a : supernodelist) {
          distance.put(calculateDistance(a, nodelist.get(j)), a);
        }
        Object[] keys = distance.keySet().toArray();
        Arrays.sort(keys);
        if (distance.get(keys[0]).getRegionID() == 0) {
          nodelist.get(j).setRegionID(0);
          nodelist.get(j).getRoutingTable().addNeighbor(distance.get(keys[0]));
        } else if (distance.get(keys[0]).getRegionID() == 1) {
          nodelist.get(j).setRegionID(1);
          nodelist.get(j).getRoutingTable().addNeighbor(distance.get(keys[0]));
        } else if (distance.get(keys[0]).getRegionID() == 2) {
          nodelist.get(j).setRegionID(2);
          nodelist.get(j).getRoutingTable().addNeighbor(distance.get(keys[0]));
        } else if (distance.get(keys[0]).getRegionID() == 3) {
          nodelist.get(j).setRegionID(3);
          nodelist.get(j).getRoutingTable().addNeighbor(distance.get(keys[0]));
        } else if (distance.get(keys[0]).getRegionID() == 4) {
          nodelist.get(j).setRegionID(4);
          nodelist.get(j).getRoutingTable().addNeighbor(distance.get(keys[0]));
        } else if (distance.get(keys[0]).getRegionID() == 5) {
          nodelist.get(j).setRegionID(5);
          nodelist.get(j).getRoutingTable().addNeighbor(distance.get(keys[0]));
        } else if (distance.get(keys[0]).getRegionID() == 6) {
          nodelist.get(j).setRegionID(6);
          nodelist.get(j).getRoutingTable().addNeighbor(distance.get(keys[0]));
        }
      }

      int i = 0;

      while (i < nodelist.size()-1) {
        Random random = new Random();
        int randomValue;

        while (nodelist.get(i).getRoutingTable().getOutbound().size() < nodelist.get(i).getNumConnection()) {
          do {
            randomValue = random.nextInt(nodelist.size());
          } while (nodelist.get(randomValue) == nodelist.get(i)
              & !holderlist.contains(nodelist.get(randomValue).getIP()));

          if (holderlist.size() == nodelist.size() - 1) {
            System.out
                .println("Can't complete node id: " + nodelist.get(i).getNodeID()
                    + "'s outgoing connection to " + nodelist.get(i).getNumConnection() + " Final count was: "
                    + nodelist.get(i).getRoutingTable().getOutbound().size() + ". Moving on to next node\n");
            i++;
            holderlist.clear();
            if (i == nodelist.size()) {
              continue;
            }
          }

          if (calculateDistance(nodelist.get(i), nodelist.get(randomValue)) > 0
              & nodelist.get(i).getRegionID() == nodelist.get(randomValue).getRegionID()) {
            nodelist.get(i).getRoutingTable().addNeighbor(nodelist.get(randomValue));
            holderlist.add(nodelist.get(randomValue).getIP());

            if (nodelist.get(i).getRoutingTable().getOutbound().size() == nodelist.get(i).getNumConnection()) {
              i++;
              holderlist.clear();
              break;
            }

          } else {
            holderlist.add(nodelist.get(randomValue).getIP());
          }
        }
      }
    }
  }

  /**
   * Adds the provided node to the list of outbound connections of self node.The
   * provided node
   * will not be added if it is the self node, it exists as an outbound connection
   * of the self node,
   * it exists as an inbound connection of the self node or the self node does not
   * allow for
   * additional outbound connections. Otherwise, the self node will add the
   * provided node to the
   * list of outbound connections and the provided node will add the self node to
   * the list of
   * inbound connections.
   *
   * @param node the node to be connected to the self node.
   * @return the success state
   */
  public boolean addNeighbor(Node node) {
    if (node == getSelfNode() || this.outbound.contains(node) || this.inbound.contains(
        node) || this.outbound.size() >= this.getNumConnection()) {
      return false;
    } else if (this.outbound.add(node) && node.getRoutingTable().addInbound(getSelfNode())) {
      printAddLink(node);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Remove the provided node from the list of outbound connections of the self
   * node and the
   * self node from the list inbound connections from the provided node.
   *
   * @param node the node to be disconnected from the self node.
   * @return the success state of the operation
   */
  public boolean removeNeighbor(Node node) {
    if (this.outbound.remove(node) && node.getRoutingTable().removeInbound(getSelfNode())) {
      printRemoveLink(node);
      return true;
    }
    return false;
  }

  /**
   * Adds the provided node as an inbound connection.
   *
   * @param from the node to be added as an inbound connection
   * @return the success state of the operation
   */
  public boolean addInbound(Node from) {
    if (this.inbound.add(from)) {
      printAddLink(from);
      return true;
    }
    return false;
  }

  public boolean addOutbound(Node to) {
    if (this.outbound.add(to)) {
      printAddLink(to);
      return true;
    }
    return false;
  }

  /**
   * Removes the provided node as an inbound connection.
   *
   * @param from the node to be removed as an inbound connection
   * @return the success state of the operation
   */
  public boolean removeInbound(Node from) {
    if (this.inbound.remove(from)) {
      printRemoveLink(from);
      return true;
    }
    return false;
  }

  public boolean removeOutbound(Node to) {
    if (this.outbound.remove(to)) {
      printRemoveLink(to);
      return true;
    }
    return false;
  }

  public void reconnectAll(long[][] matrix, int numNodes) {
    HashMap<Long, Node> map = new HashMap<>();
    int j = this.getSelfNode().getNodeID();
    for (int i = 0; i < numNodes; i++) {
      if (i == j || matrix[i][j] == 0) {
        continue;
      } else {
        map.put(matrix[i][j], getSimulatedNodes().get(i));
        matrix[i][j] = 0;
      }
    }

    ArrayList<Long> sortedKeys = new ArrayList<Long>(map.keySet());

    Collections.sort(sortedKeys);

    if (!(sortedKeys.size() == this.getSelfNode().getRoutingTable().getInbound().size())) {

      int difference = this.getSelfNode().getRoutingTable().getInbound().size() - sortedKeys.size();

      getSimulatedNodes().get(j).getRoutingTable().removeInbounds(getSimulatedNodes().get(j));

      for (int i = 0; i < sortedKeys.size(); i++) {
        map.get(sortedKeys.get(i)).getRoutingTable().addNeighbor(getSimulatedNodes().get(j));
      }

      boolean flag = false;
      ArrayList<Integer> candidates = new ArrayList<>();
      for (int i = 0; i < getSimulatedNodes().size(); i++) {
        if (getSimulatedNodes().get(i).getRoutingTable().getOutbound().size() < 8) {
          candidates.add(i);
          flag = true;
        }
      }

      if (flag = true) {
        Collections.shuffle(candidates);
        for (int k = 0; k < difference; k++) {
          if (k < candidates.size())
            getSimulatedNodes().get(candidates.get(k)).addNeighbor(getSimulatedNodes().get(j));
        }
      }

    } else {
      map.get(sortedKeys.get(sortedKeys.size() - 1)).getRoutingTable().removeOutbound(getSimulatedNodes().get(j));
      getSimulatedNodes().get(j).getRoutingTable().removeInbound(map.get(sortedKeys.get(sortedKeys.size() - 1)));

      boolean flag = false;
      ArrayList<Integer> candidates = new ArrayList<>();
      for (int i = 0; i < getSimulatedNodes().size(); i++) {
        if (getSimulatedNodes().get(i).getRoutingTable().getOutbound().size() < 8) {
          candidates.add(i);
          flag = true;
        }
      }
      if (flag = true) {
        Collections.shuffle(candidates);
        getSimulatedNodes().get(candidates.get(0)).addNeighbor(getSimulatedNodes().get(j));
      }
    }
  }

  public int calculateDistance(Node from, Node to) {
    final double AVERAGE_RADIUS_OF_EARTH = 6371;

    double latDistance = Math
        .toRadians(from.getLatitude() - to.getLatitude());
    double lngDistance = Math
        .toRadians(from.getLongitude() - to.getLongitude());

    double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
        (Math.cos(Math.toRadians(from.getLatitude()))) *
            (Math.cos(Math.toRadians(to.getLongitude()))) *
            (Math.sin(lngDistance / 2)) *
            (Math.sin(lngDistance / 2));

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH * c));
  }

  // TODO add example
  private void printAddLink(Node endNode) {
    OUT_JSON_FILE.print("{");
    OUT_JSON_FILE.print("\"kind\":\"add-link\",");
    OUT_JSON_FILE.print("\"content\":{");
    OUT_JSON_FILE.print("\"timestamp\":" + getCurrentTime() + ",");
    OUT_JSON_FILE.print("\"begin-node-id\":" + getSelfNode().getNodeID() + ",");
    OUT_JSON_FILE.print("\"end-node-id\":" + endNode.getNodeID());
    OUT_JSON_FILE.print("}");
    OUT_JSON_FILE.print("},");
    OUT_JSON_FILE.flush();
  }

  // TODO add example
  private void printRemoveLink(Node endNode) {
    OUT_JSON_FILE.print("{");
    OUT_JSON_FILE.print("\"kind\":\"remove-link\",");
    OUT_JSON_FILE.print("\"content\":{");
    OUT_JSON_FILE.print("\"timestamp\":" + getCurrentTime() + ",");
    OUT_JSON_FILE.print("\"begin-node-id\":" + getSelfNode().getNodeID() + ",");
    OUT_JSON_FILE.print("\"end-node-id\":" + endNode.getNodeID());
    OUT_JSON_FILE.print("}");
    OUT_JSON_FILE.print("},");
    OUT_JSON_FILE.flush();
  }

}