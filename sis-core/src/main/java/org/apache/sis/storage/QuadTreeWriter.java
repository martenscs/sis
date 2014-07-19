/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.sis.storage;

//JDK imports
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class to save the quad tree index from file.
 * 
 */
public class QuadTreeWriter {

  /**
   * Writes the entire quad tree index to file with each node in saved in a
   * separate file.
   * 
   * @param tree
   * @param directory
   */
  public static void writeTreeToFile(QuadTree tree, String directory) {
    createIdxDir(directory);
    writeTreeConfigsToFile(tree, directory);
    writeNodeToFile(tree.getRoot(), directory);
  }

  private static void createIdxDir(String directory) {
    File dir = new File(directory);
    if (!dir.exists()) {
      System.out.println("[INFO] Creating qtree idx dir: [" + directory + "]");
      new File(directory).mkdirs();
    }
  }

  private static void writeTreeConfigsToFile(QuadTree tree, String directory) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(directory
          + "tree_config.txt"));
      writer.write("capacity;" + tree.getCapacity() + ";depth;"
          + tree.getDepth());
      writer.newLine();
      writer.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private static void writeNodeToFile(QuadTreeNode node, String directory) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(directory
          + "node_" + node.getId() + ".txt"));

      if (node.getNodeType() == NodeType.GRAY) {
        if (node.getChild(Quadrant.NW) != null) {
          writer.write(getQuadTreeDataString(Quadrant.NW, node
              .getChild(Quadrant.NW)));
          writer.newLine();
        }

        if (node.getChild(Quadrant.NE) != null) {
          writer.write(getQuadTreeDataString(Quadrant.NE, node
              .getChild(Quadrant.NE)));
          writer.newLine();
        }

        if (node.getChild(Quadrant.SW) != null) {
          writer.write(getQuadTreeDataString(Quadrant.SW, node
              .getChild(Quadrant.SW)));
          writer.newLine();
        }

        if (node.getChild(Quadrant.SE) != null) {
          writer.write(getQuadTreeDataString(Quadrant.SE, node
              .getChild(Quadrant.SE)));
          writer.newLine();
        }
      }
      writer.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (node.getNodeType() == NodeType.GRAY) {
      if (node.getChild(Quadrant.NW) != null
          && node.getChild(Quadrant.NW).getNodeType() == NodeType.GRAY) {
        writeNodeToFile(node.getChild(Quadrant.NW), directory);
      }

      if (node.getChild(Quadrant.NE) != null
          && node.getChild(Quadrant.NE).getNodeType() == NodeType.GRAY) {
        writeNodeToFile(node.getChild(Quadrant.NE), directory);
      }

      if (node.getChild(Quadrant.SW) != null
          && node.getChild(Quadrant.SW).getNodeType() == NodeType.GRAY) {
        writeNodeToFile(node.getChild(Quadrant.SW), directory);
      }

      if (node.getChild(Quadrant.SE) != null
          && node.getChild(Quadrant.SE).getNodeType() == NodeType.GRAY) {
        writeNodeToFile(node.getChild(Quadrant.SE), directory);
      }
    }
  }

  private static String getQuadTreeDataString(Quadrant quadrant,
      final QuadTreeNode node) {
    StringBuffer str = new StringBuffer();
    str.append(quadrant.index());
    str.append(':');
    str.append(node.getNodeType().toString());
    str.append(':');
    str.append(node.getId());
    str.append(':');
    str.append(node.getCapacity());
    str.append(':');
    QuadTreeData[] data = node.getData();
    for (int i = 0; i < node.getCount(); i++) {
      str.append(data[i].getLatLon().getLatitude());
      str.append(';');
      str.append(data[i].getLatLon().getLongitude());
      str.append(';');
      str.append(data[i].getFileName());
      str.append(':');
    }
    return str.substring(0, str.length() - 1);
  }
}
