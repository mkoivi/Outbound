package outbound.ai.outbound;

import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;
import java.util.List;

public class Metar {

	String station;
	String title;
	String time;
	int temp = -274;
	int dewPoint = -274;
	int qnh = -1;
	int meanWindSpeed = -1;
	int meanWindDirection =-1;
	int gustWindSpeed = -1;
	int windVarMin = -1;
	int windVarMax = -1;
	int visibility = -1;
	String presentWeather = "";
	List<CloudLayer> clouds = new LinkedList<>();
	boolean cavok=false;
	boolean cb = false;

	String message;

	LatLng location;

	public String getStation() {
		return station;
	}


	public void setStation(String station) {
		this.station = station;
	}


	public String getTime() {
		return time;
	}


	public void setTime(String time) {
		this.time = time;
	}


	public int getTemp() {
		return temp;
	}


	public void setTemp(int temp) {
		this.temp = temp;
	}


	public int getDewPoint() {
		return dewPoint;
	}


	public void setDewPoint(int dewPoint) {
		this.dewPoint = dewPoint;
	}


	public int getMeanWindSpeed() {
		return meanWindSpeed;
	}


	public void setMeanWindSpeed(int meanWindSpeed) {
		this.meanWindSpeed = meanWindSpeed;
	}


	public int getMeanWindDirection() {
		return meanWindDirection;
	}


	public void setMeanWindDirection(int meanWindDirection) {
		this.meanWindDirection = meanWindDirection;
	}


	public int getGustWindSpeed() {
		return gustWindSpeed;
	}


	public void setGustWindSpeed(int gustWindSpeed) {
		this.gustWindSpeed = gustWindSpeed;
	}


	public int getWindVarMin() {
		return windVarMin;
	}


	public void setWindVarMin(int windVarMin) {
		this.windVarMin = windVarMin;
	}


	public int getWindVarMax() {
		return windVarMax;
	}


	public void setWindVarMax(int windVarMax) {
		this.windVarMax = windVarMax;
	}


	public int getVisibility() {
		return visibility;
	}


	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}


	public List<CloudLayer> getClouds() {
		return clouds;
	}


	public void setClouds(List<CloudLayer> clouds) {
		this.clouds = clouds;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public int getQnh() {
		return qnh;
	}


	public void setQnh(int qnh) {
		this.qnh = qnh;
	}


	public boolean isCavok() {
		return cavok;
	}


	public void setCavok(boolean cavok) {
		this.cavok = cavok;
	}


	public String getPresentWeather() {
		return presentWeather;
	}


	public void setPresentWeather(String presentWeather) {
		this.presentWeather = presentWeather;
	}


	public LatLng getLocation() {
		return location;
	}

	public void setLocation(LatLng location) {
		this.location = location;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isCb() {
		return cb;
	}

	public void setCb(boolean cb) {
		this.cb = cb;
	}

	public int getCloudBase() {
		int cloudBase = 50;
		if( cavok )
			return 50;
		else if( clouds.size() == 0) {
			return -1;
		}
		for( CloudLayer cl:clouds) {
			if(cl.layerType.equals("BKN") || cl.layerType.equals("OVC")) {
				if( cl.baseHeight < cloudBase)
					cloudBase = cl.baseHeight;
			}
		}
		return cloudBase;
	}
}
