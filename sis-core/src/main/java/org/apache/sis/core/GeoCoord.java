package org.apache.sis.core;

public class GeoCoord {
	double lat, lon; // decimal degrees latitude and longitude
	int latd, latm, lond, lonm; // latitude degrees and minutes, longitude
								// degrees and minutes
	double lats;
	double lons; // latitude and longitude seconds.

	public GeoCoord() {
		lat = 0;
		lon = 0;
		latd = 0;
		lond = 0;
		latm = 0;
		lonm = 0;
		lats = 0;
		lons = 0;
	}

	// construct from decimal degree input lat, lon
	public GeoCoord(double x, double y) {
		lat = x;
		lon = y;
		DecToDMS();
	}

	// construct from degrees minutes sec input (lat, lon)
	public GeoCoord(int a, int b, double c, int d, int e, double f) {
		latd = a;
		latm = b;
		lats = c;
		lond = d;
		lonm = e;
		lons = f;
		DMSToDec();
	}

	// set coordinates in Decimal deg
	public void SetValue(double x, double y) {
		lat = x;
		lon = y;
		DecToDMS();
	}

	// set coordinates in DMS
	public void SetValue(int a, int b, double c, int d, int e, double f) {
		latd = a;
		latm = b;
		lats = c;
		lond = d;
		lonm = e;
		lons = f;
		DMSToDec();
	}

	public double lat() {
		return lat;
	}

	public double lon() {
		return lon;
	}

	public int latd() {
		return latd;
	}

	public int latm() {
		return latm;
	}

	public double lats() {
		return lats;
	}

	public int lond() {
		return lond;
	}

	public int lonm() {
		return lonm;
	}

	public double lons() {
		return lons;
	}

	void DecToDMS() {

		// change the decimal degrees to degrees minutes seconds
		double tempf;
		// first do the latitude
		tempf = java.lang.Math.abs(lat);
		latd = (int) tempf;
		tempf = (tempf - latd) * 60.0;
		latm = (int) tempf;
		lats = (tempf - latm) * 60.0;
		if (lat < 0) { // put the sign back on the degrees
			latd = -latd;
		}

		// now do the longitude
		tempf = java.lang.Math.abs(lon);
		lond = (int) tempf;
		tempf = (tempf - lond) * 60.0;
		lonm = (int) tempf;
		lons = (tempf - lonm) * 60.0;
		if (lon < 0) {
			lond = -lond;
		}
	}

	void DMSToDec() {
		// change from degrees minutes seconds to decimal degrees

		lat = java.lang.Math.abs(latd) + latm / 60.0 + lats / 3600.0;
		lat = latd < 0 ? -lat : lat;
		lon = java.lang.Math.abs(lond) + lonm / 60.0 + lons / 3600.0;
		lon = lond < 0 ? -lon : lon;
	}

	public String toString(){
		return(new String(lat + " " + lon ));
	}

	public String toDMSstring() {
		String dstr, mstr = null, sstr = null;
		String sstr1 = null;
		String latstr = null;
		String lonstr = null;
		dstr = new String(Math.abs(latd) + "d");
		if (latm < 10) {
			mstr = new String("0" + latm + "'");
		} else {
			mstr = new String(latm + "'");
		}
		if (lats < 10) {
			sstr = new String("0" + lats);
		} else {
			sstr = new String(lats + "");
		}
		try {
			sstr1 = new String(sstr.substring(0, 3) + "''");
		} catch (StringIndexOutOfBoundsException e) {
			sstr1 = new String(sstr + "''");
		}
		if (lat < 0) {
			latstr = new String(dstr + mstr + sstr1 + "S");
		} else {
			latstr = new String(dstr + mstr + sstr1 + "N");
		}
		dstr = new String(Math.abs(lond) + "d");
		if (lonm < 10) {
			mstr = new String("0" + lonm + "'");
		} else {
			mstr = new String(lonm + "'");
		}
		if (lons < 10) {
			sstr = new String("0" + lons);
		} else {
			sstr = new String(lons + "");
		}
		try {
			sstr1 = new String(sstr.substring(0, 3) + "''");
		} catch (StringIndexOutOfBoundsException e) {
			sstr1 = new String(sstr + "''");
		}
		if (lon < 0) {
			lonstr = new String(dstr + mstr + sstr1 + "W");
		} else {
			lonstr = new String(dstr + mstr + sstr1 + "E");
		}

		return (new String(latstr + " " + lonstr + " : " + lat + " " + lon));
	}

}
