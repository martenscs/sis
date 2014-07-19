/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.sis.core;

/**
 * Represents 2D point on earth surface by latitude and longitude.
 * 
 * 
 * @author cmartens
 * @version $Revision: 1.0.0 $
 */
public class LatLon extends AbstractLatLong implements LatitudeLongitude {

  /**
   * LatLon to represent geo point.
   * 
   * @param lat the latitude
   * @param lon the longitude
   */
  public LatLon(double lat, double lon) {
    super(lat, lon);
  }

  @Override
  public void updateLatitudeLongitude(LatitudeLongitude latitudeLongitude) {
    if (this.getLatitude() == 0) this.setLatitude(latitudeLongitude.getLatitude());
    if (this.getLongitude() == 0) this.setLongitude(latitudeLongitude.getLongitude());
  }
}
