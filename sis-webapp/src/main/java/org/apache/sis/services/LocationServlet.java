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

package org.apache.sis.services;

//JDK imports
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.sis.core.LatLon;
import org.apache.sis.core.LatLonRect;
import org.apache.sis.distance.DistanceUtils;
import org.apache.sis.storage.GeoRSSData;
import org.apache.sis.storage.QuadTree;
import org.apache.sis.storage.QuadTreeData;
import org.apache.sis.storage.QuadTreeReader;
import org.apache.sis.storage.QuadTreeWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

//ROME imports
import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * 
 * A location web service that loads data from GeoRSS format
 * (configured via a provided config.xml file), and then loads up 
 * a {@link QuadTree} with this information, making it queryable for callers.
 * 
 */
public class LocationServlet extends HttpServlet {

  private static final long serialVersionUID = 731743219362175102L;
  private QuadTree tree;
  private ServletContext context;
  private String timeToLoad;

  @SuppressWarnings("unchecked")
  public void init(ServletConfig config) throws ServletException {
    this.context = config.getServletContext();
    long startTime = 0;
    long endTime = 0;
    int capacity=-1, depth=-1;
    String qtreeIdxPath = this.context.getInitParameter("org.apache.sis.services.config.qIndexPath");
    String georssStoragePath = this.context.getInitParameter("org.apache.sis.services.config.geodataPath");
    
    if(!qtreeIdxPath.endsWith("/")) qtreeIdxPath+="/";
    if(!georssStoragePath.endsWith("/")) georssStoragePath+="/";
    
    InputStream indexStream = null;
    try {
      indexStream = new FileInputStream(qtreeIdxPath+"node_0.txt");
    } catch (FileNotFoundException e) {
      e.printStackTrace();      
    }
    
    if (indexStream != null) {
      startTime = System.currentTimeMillis();
      this.tree = QuadTreeReader.readFromFile(qtreeIdxPath, "tree_config.txt", "node_0.txt");
      try {
        indexStream.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      endTime = System.currentTimeMillis();
      this.timeToLoad = "Quad Tree fully loaded from index files in "
          + Double.toString((endTime - startTime) / 1000L) + " seconds";
      System.out.println("[INFO] Finished loading tree from stored index");
    } else {
      startTime = System.currentTimeMillis();
      WireFeedInput wf = new WireFeedInput(true);
      // read quad tree properties set in config xml file
      InputStream configStream = null;
      try{
        configStream = new FileInputStream(this.context.getInitParameter("org.apache.sis.services.config.filePath"));
      }
      catch(Exception e){
        e.printStackTrace();
      }
      
      if (configStream != null) {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        try {
          DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
          Document configDoc = docBuilder.parse(configStream);
          NodeList capacityNode = configDoc.getElementsByTagName("capacity");
          if (capacityNode.item(0) != null) {
            capacity = Integer.parseInt(capacityNode.item(0).getFirstChild()
                .getNodeValue());
          }

          NodeList depthNode = configDoc.getElementsByTagName("depth");
          if (depthNode.item(0) != null) {
            depth = Integer.parseInt(depthNode.item(0).getFirstChild()
                .getNodeValue());
          }
          this.tree = new QuadTree(capacity, depth); // TODO make this
          // configurable

          NodeList urlNodes = configDoc.getElementsByTagName("url");
          for (int i = 0; i < urlNodes.getLength(); i++) {
            // read in georss and build tree
            WireFeed feed = wf.build(new XmlReader(new URL(urlNodes.item(i)
                .getFirstChild().getNodeValue())));
            Channel c = (Channel) feed;
            List<Item> items = (List<Item>) c.getItems();
            for (Item item : items) {
              GeoRSSModule geoRSSModule = (GeoRSSModule) item
                  .getModule(GeoRSSModule.GEORSS_GEORSS_URI);
              if (geoRSSModule == null)
                geoRSSModule = (GeoRSSModule) item
                    .getModule(GeoRSSModule.GEORSS_GML_URI);
              if (geoRSSModule == null)
                geoRSSModule = (GeoRSSModule) item
                    .getModule(GeoRSSModule.GEORSS_W3CGEO_URI);
              // if location from the item cannot be retrieved
              // then discard it
              if (geoRSSModule != null && geoRSSModule.getPosition() != null) {
                String filename = "";
                if (item.getGuid() != null)
                  filename = cleanStr(item.getGuid().getValue()) + ".txt";
                else
                  filename = cleanStr(item.getLink()) + ".txt";

                GeoRSSData data = new GeoRSSData(filename, new LatLon(
                    geoRSSModule.getPosition().getLatitude(), geoRSSModule
                        .getPosition().getLongitude()));
                if (this.tree.insert(data)) {
                  data.saveToFile(item, geoRSSModule,
                      georssStoragePath);
                } else {
                  System.out.println("[INFO] Unable to store data at location "
                      + data.getLatLon().getLat() + ", " + data.getLatLon().getLon()
                      + " under filename " + data.getFileName());
                }
              }
            }
          }
          configStream.close();
          endTime = System.currentTimeMillis();
          this.timeToLoad = "Quad Tree fully loaded from retrieving GeoRSS files over the network in "
              + Double.toString((endTime - startTime) / 1000L) + " seconds";
          QuadTreeWriter.writeTreeToFile(tree, qtreeIdxPath);
        } catch (ParserConfigurationException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (SAXException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IllegalArgumentException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (FeedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } else {
        throw new ServletException("Unable to read location service XML config: null!");
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    long beforeTime = 0;
    long afterTime = 0;

    response.setContentType("text/xml");
    PrintWriter out = response.getWriter();

    String type = request.getParameter("type");

    List<QuadTreeData> results = new ArrayList<QuadTreeData>();
    List<String> regions = new ArrayList<String>();
    if (type != null && type.equals("bbox")) {
      String llLat = request.getParameter("llLat");
      String llLon = request.getParameter("llLon");
      String urLat = request.getParameter("urLat");
      String urLon = request.getParameter("urLon");

      if (llLat != null && llLon != null && urLat != null && urLon != null) {
        try {
          LatLonRect bbox = new LatLonRect(new LatLon(
              Double.parseDouble(llLat), Double.parseDouble(llLon)),
              new LatLon(Double.parseDouble(urLat), Double.parseDouble(urLon)));

          beforeTime = System.currentTimeMillis();
          results = tree.queryByBoundingBox(bbox);
          afterTime = System.currentTimeMillis();
          // get the polygon that approximates the region
          Rectangle2D[] rects = bbox.getJavaRectangles();
          for (int i = 0; i < rects.length; i++) {
            String regionStr = (rects[i].getMinY() - 90) + ","
                + (rects[i].getMinX() - 180) + ",";
            regionStr += (rects[i].getMaxY() - 90) + ","
                + (rects[i].getMinX() - 180) + ",";
            regionStr += (rects[i].getMaxY() - 90) + ","
                + (rects[i].getMaxX() - 180) + ",";
            regionStr += (rects[i].getMinY() - 90) + ","
                + (rects[i].getMaxX() - 180) + ",";
            regionStr += (rects[i].getMinY() - 90) + ","
                + (rects[i].getMinX() - 180);
            regions.add(regionStr);
          }
        } catch (NumberFormatException ex) {
          System.out
              .println("[ERROR] Input parameters were not valid latitudes and longitudes");
        }

      }
    } else if (type != null && type.equals("pointradius")) {
      String radius = request.getParameter("radius");
      String lat = request.getParameter("lat");
      String lon = request.getParameter("lon");

      if (radius != null && lat != null && lon != null) {

        LatLon point = null;
        try {
          point = new LatLon(Double.parseDouble(lat), Double.parseDouble(lon));
        } catch (NumberFormatException ex) {
          System.out
              .println("{ERROR] Input parameters were not valid latitudes and longitudes");
        }

        double radiusKM = Double.parseDouble(radius);

        String regionStr = "";
        regionStr = "-90.0,-180.0, 90.0, -180.0, 90.0, 180.0, -90.0, 180.0, -90.0,-180.0";
        regions.add(regionStr);

        for (int i = 0; i < 360; i += 10) {
          LatLon pt = DistanceUtils.getPointOnGreatCircle(point.getLat(), point
              .getLon(), radiusKM, i);
          regionStr += pt.toString() + ",";
        }
        LatLon pt = DistanceUtils.getPointOnGreatCircle(point.getLat(), point
            .getLon(), radiusKM, 0);
        regionStr += pt.toString() + ",";
        regions.add(regionStr.substring(0, regionStr.length() - 1));

        beforeTime = System.currentTimeMillis();
        results = tree.queryByPointRadius(point, radiusKM);
        afterTime = System.currentTimeMillis();

      }
    }
    long timeSeconds = afterTime - beforeTime;
    // return matches from tree in xml format to client
    out.write(buildXML(results, regions, timeSeconds));
    out.close();
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    String filename = request.getParameter("filename");

    if (filename != null) {

      HashMap<String, String> map = GeoRSSData.loadFromFile(this.context
          .getRealPath("/")
          + "geodata" + File.separator + filename);
      String html = "";

      if (map.get("title") != null && !map.get("title").equals("null")) {
        html += "<b>Title:&nbsp;</b>" + map.get("title") + "<br />";
      }
      if (map.get("link") != null && !map.get("link").equals("null")) {
        html += "<b>Link:&nbsp;</b><a target='_blank' href='" + map.get("link")
            + "'" + ">" + map.get("link") + "</a><br />";
      }
      if (map.get("author") != null && !map.get("author").equals("null")) {
        html += "<b>Author:&nbsp;</b>" + map.get("author") + "<br />";
      }
      if (map.get("pubDate") != null && !map.get("pubDate").equals("null")) {
        html += "<b>Pub Date:&nbsp;</b>" + map.get("pubDate") + "<br />";
      }
      if (map.get("description") != null
          && !map.get("description").equals("null")) {
        html += "<b>Description:&nbsp;</b>" + map.get("description") + "<br />";
      }
      html += "<b>Lat:&nbsp;</b>" + map.get("lat") + "<br />";
      html += "<b>Lon:&nbsp;</b>" + map.get("lon") + "<br />";

      out.write(html);

    }
    out.close();
  }

  /**
   * Builds the XML file to return to client.
   * 
   * @param filterList
   *          list of QuadTreeData that are within the search region
   * @param regions
   *          the String coordinate representation of the search region
   * @param time
   *          the time it took to execute the query
   * @return XML string
   */
  private String buildXML(final List<QuadTreeData> filterList,
      final List<String> regions, final long time) {
    DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();

    try {
      DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
      Document doc = docBuilder.newDocument();

      Element root = doc.createElement("root");
      doc.appendChild(root);
      for (QuadTreeData geo : filterList) {
        Element item = doc.createElement("item");

        Element id = doc.createElement("id");
        Text idText = doc.createTextNode(geo.getFileName());
        id.appendChild(idText);
        item.appendChild(id);

        Element lat = doc.createElement("lat");
        Text latText = doc.createTextNode(Double.toString(geo.getLatLon().getLat()));
        lat.appendChild(latText);
        item.appendChild(lat);

        Element lon = doc.createElement("lon");
        Text lonText = doc.createTextNode(Double.toString(geo.getLatLon().getLon()));
        lon.appendChild(lonText);
        item.appendChild(lon);

        root.appendChild(item);
      }

      Element timeElem = doc.createElement("time");
      Text timeText = doc.createTextNode(Long.toString(time));
      timeElem.appendChild(timeText);
      root.appendChild(timeElem);

      if (timeToLoad != null) {
        Element indexLoadTimeElem = doc.createElement("indexLoadTime");
        Text indexLoadTimeText = doc.createTextNode(timeToLoad);
        indexLoadTimeElem.appendChild(indexLoadTimeText);
        root.appendChild(indexLoadTimeElem);
        timeToLoad = null; // Only need to send this over to the client
        // on initial load
      }

      Element query = doc.createElement("query");
      root.appendChild(query);
      for (String rStr : regions) {
        Element region = doc.createElement("region");
        Text regionText = doc.createTextNode(rStr);
        region.appendChild(regionText);
        query.appendChild(region);
      }
      try {
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(doc);
        trans.transform(source, result);
        return sw.toString();
      } catch (TransformerConfigurationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (TransformerException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (ParserConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;

  }

  private static String cleanStr(String id) {
    String cleanedID = id;
    return cleanedID.replaceAll("[^a-zA-Z0-9]", "");
  }
}
