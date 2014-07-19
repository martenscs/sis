package org.apache.sis.core;

public interface LatitudeLongitude {

  /**
   * Shifts the latitude by +90.0 so that all latitude lies in the positive coordinate. Used mainly
   * for Java 2D geometry.
   * 
   * @return latitude shifted by +90.0
   */
  public abstract double getShiftedLat();

  /**
   * Shifts the longitude by +180.0 so that all longitude lies in the positive coordinate. Used
   * mainly for Java 2D geometry.
   * 
   * @return longitude shifted by +180.0
   */
  public abstract double getShiftedLon();

  /**
   * Returns the latitude.
   * 
   * @return latitude
   */
  public abstract double getLatitude();

  /**
   * Returns the longitude.
   * 
   * @return longitude
   */
  public abstract double getLongitude();

  /**
   * Normalizes the longitude values to be between -180.0 and 180.0
   * 
   * @return longitude value that is between -180.0 and 180.0 inclusive
   */
  public abstract double getNormLon();

  void setLongitude(double longitude);

  void setLatitude(double latitude);

  public String getLatitudeISO6709DMS(double latitude);

  public String getLongitudeISO6709DMS(double longitude);

  public String getLongitudeDMSNoUnicode(double logitude);

  public String getLatitudeDMSNoUnicode(double latitude);

  public void updateLatitudeLongitude(LatitudeLongitude latitudeLongitude);
}
