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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The type Network configuration allows to configure network latency and
 * bandwidth.
 */
public class NetworkConfiguration {
    /**
     * Regions where nodes can exist.
     */
    public static final List<String> REGION_LIST = new ArrayList<>(
            Arrays.asList("America", "Europe", "Asia", "Australia", "Africa",
                    "Atlantic", "Pacific"));

        public static final long[][] LATENCY = {
            { 102, 146, 291, 260, 285, 200, 313},
            { 146, 39, 178, 283, 206, 63, 320},
            { 291, 178, 191, 252, 307, 211, 329},
            { 260, 283, 252, 45, 472, 323, 68},
            { 285, 206, 307, 472, 125, 175, 453},
            { 200, 63, 211, 323, 175, 132, 300},
            { 313, 320, 329, 68, 453, 300, 259}
    };

    public static final long[] DOWNLOAD_BANDWIDTH = {
        91717209, 83048043, 82312500, 52188000 ,
        20602500, 63973333, 174985000
    };

    public static final long[] UPLOAD_BANDWIDTH = {
        28977209, 48203260, 58036944, 18060000,
        15537500, 57480000, 57875000
};

}