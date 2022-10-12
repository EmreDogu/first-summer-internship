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

package simblock.settings;

/**
 * The type Simulation configuration allows for specific simulation instance configuration.
 */
public class SimulationConfiguration {

  /**
   * The expected value of block generation interval. The difficulty of mining is automatically
   * adjusted by this value and the sum of mining power. (unit: millisecond)
   */
  public static final long INTERVAL = 1000 * 60 * 10;//1000*60;//1000*30*5;//1000*60*10;

  /**
   * The average mining power of each node. Mining power corresponds to Hash Rate in Bitcoin, and
   * is the number of mining (hash calculation) executed per millisecond.
   */
  public static final int AVERAGE_MINING_POWER = 400000;

  /**
   * The mining power of each node is determined randomly according to the normal distribution
   * whose average is AVERAGE_MINING_POWER and standard deviation is STDEV_OF_MINING_POWER.
   */
  public static final int STDEV_OF_MINING_POWER = 100000;

  /**
   * Block size. (unit: byte).
   */
  public static final long BLOCK_SIZE = 535000;//6110;//8000;//535000;//0.5MB
}
