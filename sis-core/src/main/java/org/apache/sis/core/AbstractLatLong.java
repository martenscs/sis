package org.apache.sis.core;

public abstract class AbstractLatLong implements LatitudeLongitude {

  protected double latitude;
  protected double longitude;

  /**
   * LatLon to represent geo point.
   * 
   * @param lat the latitude
   * @param lon the longitude
   */
  public AbstractLatLong(double lat, double lon) {
    this.latitude = lat;
    this.longitude = lon;
  }

  public AbstractLatLong() {
    super();
  }

  /**
   * Shifts the latitude by +90.0 so that all latitude lies in the positive coordinate. Used mainly
   * for Java 2D geometry.
   * 
   * @return latitude shifted by +90.0
   */
  @Override
  public double getShiftedLat() {
    return getLatitude() + 90.0;
  }

  /**
   * Shifts the longitude by +180.0 so that all longitude lies in the positive coordinate. Used
   * mainly for Java 2D geometry.
   * 
   * @return longitude shifted by +180.0
   */
  @Override
  public double getShiftedLon() {
    return getLongitude() + 180.0;
  }

  /**
   * Returns the latitude.
   * 
   * @return latitude
   */
  @Override
  public double getLatitude() {
    return this.latitude;
  }

  /**
   * Returns the longitude.
   * 
   * @return longitude
   */
  @Override
  public double getLongitude() {
    return this.longitude;
  }

  /**
   * Normalizes the longitude values to be between -180.0 and 180.0
   * 
   * @return longitude value that is between -180.0 and 180.0 inclusive
   */
  @Override
  public double getNormLon() {
    double normLon = getLongitude();
    if (normLon > 180.0) {
      while (normLon > 180.0) {
        normLon -= 360.0;
      }
    } else if (normLon < -180.0) {
      while (normLon < -180.0) {
        normLon += 360.0;
      }
    }
    return normLon;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("(\"GEO Position\":{ "); //$NON-NLS-1$
    sb.append("\"latitude\":"); //$NON-NLS-1$
    sb.append(getLatitudeISO6709DMS(getLatitude())); //$NON-NLS-1$  
    sb.append(",\"latitude\":"); //$NON-NLS-1$
    sb.append(getLongitudeISO6709DMS(getLongitude())); //$NON-NLS-1$
    sb.append("})"); //$NON-NLS-1$
    return sb.toString();
  }

  @Override
  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  @Override
  public void setLatitude(double latitude) {
    this.latitude = latitude;

  }

  public String getLatitudeDMSNoUnicode(double lat) {
    // change the decimal degrees to degrees minutes seconds
    double tempf;
    // first do the latitude

    tempf = java.lang.Math.abs(lat);
    int latd = (int) tempf;
    tempf = (tempf - latd) * 60.0;
    int latm = (int) tempf;
    double lats = (tempf - latm) * 60.0;
    if (lat < 0) { // put the sign back on the degrees
      latd = -latd;
    }
    String dstr, mstr = null, sstr = null;
    String sstr1 = null;
    String latstr = null;
    dstr = new String(Math.abs(latd) + "");
    if (latm < 10) {
      mstr = new String("0" + latm + "");
    } else {
      mstr = new String(latm + "");
    }
    if (lats < 10) {
      sstr = new String("0" + lats);
    } else {
      sstr = new String(lats + "");
    }
    try {
      sstr1 = new String(sstr.substring(0, 2) + "");
    } catch (StringIndexOutOfBoundsException e) {
      sstr1 = new String(sstr + "");
    }
    if (lat < 0) {
      latstr = new String(dstr + mstr + sstr1 + "S");
    } else {
      latstr = new String(dstr + mstr + sstr1 + "N");
    }
    return latstr;
  }


  public String getLongitudeDMSNoUnicode(double logitude) {
    // change the decimal degrees to degrees minutes seconds
    double tempf;

    String dstr, mstr = null, sstr = null;
    String sstr1 = null;
    String lonstr = null;
    // now do the longitude
    tempf = java.lang.Math.abs(logitude);
    int lond = (int) tempf;
    tempf = (tempf - lond) * 60.0;
    int lonm = (int) tempf;
    double lons = (tempf - lonm) * 60.0;
    if (logitude < 0) {
      lond = -lond;
    }
    dstr = new String(Math.abs(lond) + "");
    if (lonm < 10) {
      mstr = new String("0" + lonm + "");
    } else {
      mstr = new String(lonm + "");
    }
    if (lons < 10) {
      sstr = new String("0" + lons);
    } else {
      sstr = new String(lons + "");
    }
    try {
      sstr1 = new String(sstr.substring(0, 2) + "");
    } catch (StringIndexOutOfBoundsException e) {
      sstr1 = new String(sstr + "");
    }
    if (logitude < 0) {
      lonstr = new String(dstr + mstr + sstr1 + "W");
    } else {
      lonstr = new String(dstr + mstr + sstr1 + "E");
    }

    return lonstr;
  }

  @Override
  public String getLatitudeISO6709DMS(double lat) {
    // change the decimal degrees to degrees minutes seconds
    double tempf;
    // first do the latitude

    tempf = java.lang.Math.abs(lat);
    int latd = (int) tempf;
    tempf = (tempf - latd) * 60.0;
    int latm = (int) tempf;
    double lats = (tempf - latm) * 60.0;
    if (lat < 0) { // put the sign back on the degrees
      latd = -latd;
    }
    String dstr, mstr = null, sstr = null;
    String sstr1 = null;
    String latstr = null;
    dstr = new String(Math.abs(latd) + "\u00B0");
    if (latm < 10) {
      mstr = new String("0" + latm + "\u0027");
    } else {
      mstr = new String(latm + "\u0027");
    }
    if (lats < 10) {
      sstr = new String("0" + lats);
    } else {
      sstr = new String(lats + "");
    }
    try {
      sstr1 = new String(sstr.substring(0, 5) + "\"");
    } catch (StringIndexOutOfBoundsException e) {
      sstr1 = new String(sstr + "\"");
    }
    if (lat < 0) {
      latstr = new String(dstr + mstr + sstr1 + "S");
    } else {
      latstr = new String(dstr + mstr + sstr1 + "N");
    }
    return latstr;
  }

  @Override
  public String getLongitudeISO6709DMS(double logitude) {
    // change the decimal degrees to degrees minutes seconds
    double tempf;

    String dstr, mstr = null, sstr = null;
    String sstr1 = null;
    String lonstr = null;
    // now do the longitude
    tempf = java.lang.Math.abs(logitude);
    int lond = (int) tempf;
    tempf = (tempf - lond) * 60.0;
    int lonm = (int) tempf;
    double lons = (tempf - lonm) * 60.0;
    if (logitude < 0) {
      lond = -lond;
    }
    dstr = new String(Math.abs(lond) + "\u00B0");
    if (lonm < 10) {
      mstr = new String("0" + lonm + "\u0027");
    } else {
      mstr = new String(lonm + "\u0027");
    }
    if (lons < 10) {
      sstr = new String("0" + lons);
    } else {
      sstr = new String(lons + "");
    }
    try {
      sstr1 = new String(sstr.substring(0, 5) + "\"");
    } catch (StringIndexOutOfBoundsException e) {
      sstr1 = new String(sstr + "\"");
    }
    if (logitude < 0) {
      lonstr = new String(dstr + mstr + sstr1 + "W");
    } else {
      lonstr = new String(dstr + mstr + sstr1 + "E");
    }

    return lonstr;
  }

  @Override
  public void updateLatitudeLongitude(LatitudeLongitude latitudeLongitude) {
    if (this.getLatitude() == 0) this.setLatitude(latitudeLongitude.getLatitude());
    if (this.getLongitude() == 0) this.setLongitude(latitudeLongitude.getLongitude());
  }
}
