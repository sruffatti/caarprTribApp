import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * TribApp is an application developed for the Alliance. The Alliance wants the alderman, ward, police beat, police district for a
 * corresponding record.
 * <p>
 * @author Sean Ruffatti
 *@version 1.2
 */
public class TribApp2 {

	public class Methods {

	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	//@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		
		//Variable Declaration & Object Instantiation
		Scanner in = new Scanner(System.in);
		String url = "";
		String formattedAddress = "";
		String apiKey = "";
		double lat = 0;
		double lng = 0;
		String alderman;
		String ward;
		String beat;
		String district;
		
		//Prompt for input
		System.out.println("Please enter an address: '1 East Jackson Chicago Il'");
		String address = in.nextLine();
		String[ ] splitAddress = address.split(" ");
		in.close();
		
		//Format Address
		formattedAddress = formatAddress(splitAddress);
		
		//Build Google URL
		url = buildGoogleURL(formattedAddress, apiKey);
		URL googleURL = new URL(url);
	
		//Retrieve LAT & LNG
		double[ ] geo = retrieveLatLng(googleURL);
		lat = geo[0];
		lng = geo[1];
		
		//Build URL for Police Beat
		String b = buildPoliceBeatURL(lat, lng);
		URL beatURL = new URL(b);
		
		//Retrieve Police Beat
		beat = retrievePoliceBeat(beatURL);
		
		//Build URL for Police Districts
		String d = buildPoliceDistrictURL(lat, lng);
		URL districtURL = new URL(d);
		
		//Retrieve police district
		district = retrievePoliceDistrict(districtURL);
		
		
		//Build Trib Url for Wards
		String x = buildWardURL(lat, lng);
		URL tribWardURL = new URL(x);
		
		//Retrieve Alderman and Ward
		String[ ] wardValues = retrieveAldermanWard(tribWardURL);
		alderman = wardValues[0];
		ward = wardValues[1];
		
		//Print all fields to the console
		System.out.printf("Lat: %s\nLng: %s\nAlderman: %s\nWard: %s\nPolice Beat: %s\nPolice District: %s", lat, lng, alderman, ward, beat, district);
}
	
	/**
	 * Method to retrieve JSON value for Police District
	 * @param URL districtURL
	 * @return String
	 * @throws IOException 
	 */
	public static String retrievePoliceDistrict(URL districtURL) throws IOException {
		InputStream is = districtURL.openStream();
		JsonReader rdr = Json.createReader(is);
		JsonObject obj = rdr.readObject();
		JsonArray results = obj.getJsonArray("objects");
		String district = "";
		for(JsonObject result : results.getValuesAs(JsonObject.class)) {
			JsonObject x1 = result.getJsonObject("metadata");
			district = x1.getJsonString("DIST_NUM").getString();
		}
		return district;
	}
	
	/**
	 * Method to retrieve JSON value for Police Beat
	 * @param URL beatURL
	 * @return String
	 * @throws IOException 
	 */
	public static String retrievePoliceBeat(URL beatURL) throws IOException {
		InputStream is = beatURL.openStream();
		JsonReader rdr = Json.createReader(is);
		JsonObject obj = rdr.readObject();
		JsonArray results = obj.getJsonArray("objects");
		String beat = "";
		for(JsonObject result : results.getValuesAs(JsonObject.class)) {
			JsonObject x1 = result.getJsonObject("metadata");
			beat = x1.getJsonString("BEAT_NUM").getString();
		}
		return beat;
	}
	/**
	 * @param tribAppURL
	 * @throws IOException
	 */
	public static String[] retrieveAldermanWard(URL tribAppURL) throws IOException {
		InputStream is = tribAppURL.openStream();
		JsonReader rdr = Json.createReader(is);
		JsonObject obj = rdr.readObject();
		JsonArray results = obj.getJsonArray("objects");
		String[ ] values = new String[2];
		for(JsonObject result : results.getValuesAs(JsonObject.class)) {
			JsonObject x1 = result.getJsonObject("metadata");
			values[0] = x1.getJsonString("ALDERMAN").getString();
			values[1] = x1.getJsonString("WARD").getString();
		}
		return values;
	}
	/**
	 * Method is used to obtain lat and lng from google geocode json file
	 * @param googleURL
	 * @return 
	 * @throws IOException
	 */
	private static double[] retrieveLatLng(URL googleURL) throws IOException {
		InputStream is = googleURL.openStream();
		JsonReader rdr = Json.createReader(is);
		JsonObject obj = rdr.readObject();
		JsonArray results = obj.getJsonArray("results");
		double[ ] latLong = new double[2];
		
		for(JsonObject result : results.getValuesAs(JsonObject.class)) {
			JsonObject x = result.getJsonObject("geometry");
			
			latLong[0] = x.getJsonObject("location").getJsonNumber("lat").doubleValue();
			latLong[1] = x.getJsonObject("location").getJsonNumber("lng").doubleValue();
		}
		return latLong;
	}
	
	/**
	 * Method is used to format the address for the URL
	 * <p>
	 * @param address
	 * @return returns a string of the properly formatted address
	 */
	public static String formatAddress(String[ ] address) {
		String url = "";
		for(int i = 0; i < address.length; i++) {
			if(i != address.length-1) {
				url += address[i]+ "+";
			}
			else {
				url += address[i];
			}
		}
		return url;
	}
	
	/**
	 * This method receives the formatted address and apiKey for Google Geocode, then builds the URL for Google GeoCode
	 * <p>
	 * @param address
	 * @param apiKey
	 * @return a string of the formatted URL
	 */
	public static String buildGoogleURL(String address, String apiKey) {
		
		String url = "https://maps.googleapis.com/maps/api/geocode/json?address="+address+"&key=" + apiKey;
		return url;
	}
	
	/**
	 * Method is used to build the Tribune App URL
	 * <p>
	 * @param double lat & double lng
	 * @return String url
	 */
	public static String buildWardURL(double lat, double lng) {
		String url = "http://boundaries.tribapps.com/1.0/boundary/?contains="+lat+","+lng+"&sets=wards";
		return url;
	}
	
	/**
	 * Method is used to build the Police Beat URL
	 * <p>
	 * @param lat
	 * @param lng
	 * @return string
	 */
	public static String buildPoliceBeatURL(double lat, double lng) {
		String url = "http://boundaries.tribapps.com/1.0/boundary/?contains="+lat+","+lng+"&sets=police-beats";
		return url;
	}
	
	/**
	 * Method is used to build the Police District URL
	 * <p>
	 * @param lat
	 * @param lng
	 * @return String
	 */
	public static String buildPoliceDistrictURL(double lat, double lng) {
		String url = "http://boundaries.tribapps.com/1.0/boundary/?contains="+lat+","+lng+"&sets=police-districts";
		return url;
	}
}
