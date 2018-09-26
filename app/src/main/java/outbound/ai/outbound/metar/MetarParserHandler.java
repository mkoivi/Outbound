package outbound.ai.outbound.metar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import outbound.ai.outbound.CloudLayer;
import outbound.ai.outbound.Metar;

public class MetarParserHandler  extends DefaultHandler{

		public static HashMap<String, String> cloudLayerCodes = new HashMap<>();


	{
		cloudLayerCodes.put("http://codes.wmo.int/bufr4/codeflag/0-20-008/0", "CLR");
		cloudLayerCodes.put("http://codes.wmo.int/bufr4/codeflag/0-20-008/1", "FEW");
		cloudLayerCodes.put("http://codes.wmo.int/bufr4/codeflag/0-20-008/2", "SCT");
		cloudLayerCodes.put("http://codes.wmo.int/bufr4/codeflag/0-20-008/3", "BKN");
		cloudLayerCodes.put("http://codes.wmo.int/bufr4/codeflag/0-20-008/4", "OVC");

		}
	    //This is the list which shall be populated while parsing the XML.
	    private ArrayList<Metar> metarList = new ArrayList<Metar>();
	 
	    //As we read any XML element we will push that in this stack
	    private Stack elementStack = new Stack();
	 
	    //As we complete one user block in XML, we will push the User instance in userList
	    private Stack objectStack = new Stack();

	    CloudLayer cl;
	    String cloudLayerUnit = null;
		private String windSpeedUnit = null;
	    Metar metar = null;
	    
	    public void startDocument() throws SAXException
	    {
	        //System.out.println("start of the document   : ");
	    }
	 
	    public void endDocument() throws SAXException
	    {
	        //System.out.println("end of the document document     : ");
	    }
	 
	    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException
	    {
	        //Push it in element stack
	        this.elementStack.push(qName);
	 
	        if ("wfs:member".equals(qName)) 	       {
	            metar = new Metar();
	       }

	        if ("iwxxm:MeteorologicalAerodromeObservationRecord".equals(qName)) 	       {
	            metar.setCavok( Boolean.parseBoolean(attrs.getValue("cloudAndVisibilityOK")));
	       }
	        
	    	 if ("iwxxm:presentWeather".equals(qName))   {
	        	try {
					String weatherTypeUri = attrs.getValue("xlink:href");
					String weatherCode = weatherTypeUri.substring(weatherTypeUri.lastIndexOf("/") + 1, weatherTypeUri.length());
					metar.setPresentWeather(new String(weatherCode));
				}
				catch( Exception e) {
					System.err.println( "can't parse " + qName + ", attrs " + attrs );
				}
	    	 }

			if ("iwxxm:CloudLayer".equals(qName)) {
				cl = new CloudLayer();

			}

			if ("iwxxm:meanWindSpeed".equals(qName)) {
				windSpeedUnit = attrs.getValue("uom");

			}





			if ( cl != null && "iwxxm:amount".equals(qName))   {
				try {
				String cloudLayerUri = attrs.getValue("xlink:href");
				cl.layerType = cloudLayerCodes.get(cloudLayerUri);

				}
				catch( Exception e) {
					System.err.println( "can't parse " + qName + ", attrs " + attrs );
				}
			}
			if ( cl != null && "iwxxm:base".equals(qName)) {
				cloudLayerUnit = attrs.getValue("uom");
	        }
	        //Set all required attributes in any XML element here itself
	         /*   if(attributes != null && attributes.getLength() == 1)
	            {
	                metar.setId(Integer.parseInt(attributes.getValue(0)));
	            }*/
	     //      this.objectStack.push(metar);
	      //  }
//	    	String elem = currentElement();
//	    	System.out.println("start element: " + localName + ", element " + elem);
	    }
	 
	    public void endElement(String uri, String localName, String qName) throws SAXException
	    {
	        //Remove last added  element
        this.elementStack.pop();
	 
	        //User instance has been constructed so pop it from object stack and push in userList
	        if ("wfs:member".equals(qName))
	        {
	   			if( windSpeedUnit != null && windSpeedUnit.equals("m/s")) {
	   				metar.setMeanWindSpeed(Math.round(metar.getMeanWindSpeed()*1.9f));
					if( metar.getGustWindSpeed() > 0) {
						metar.setGustWindSpeed(Math.round(metar.getGustWindSpeed()*1.9f));
					}

				}

	                  this.metarList.add(metar);
	            metar = null;
	        }
			if ("iwxxm:CloudLayer".equals(qName))   {
				if(cl != null && cl.layerType != null && cl.baseHeight > 0) {
					if( cloudLayerUnit != null && cloudLayerUnit.equals("m")) {
						cl.baseHeight = Math.round( (float)cl.baseHeight / 30.48f );
						cloudLayerUnit = null;
					}
					metar.getClouds().add(cl);
				}
				else {
					System.err.println( "Could not parse CloudLayer");
				}
	        	cl = null;
			}
	//    	String elem = currentElement();
	    	
//	    	if( elem.equals("avi:input")) {
//	    		System.out.println("end element: " + localName + ", element " + elem);
//	    	}
	    	
	//    	System.out.println("end element: " + localName + ", element " + elem);
	    }
	 
	    /**
	     * This will be called everytime parser encounter a value node
	     * */
	    public void characters(char[] ch, int start, int length) throws SAXException
	    {
	        String value = new String(ch, start, length).trim();
	 
	        if (value.length() == 0)
	        {
	            return; // ignore white space
	        }
	    	String elem = currentElement();
	    	System.out.println("element " + elem);
	    	 if ("saf:locationIndicatorICAO".equals(elem)) {
	    		 metar.setStation( new String(ch, start, length) );
	    	 }
	    	 if ("saf:locationIndicatorICAO".equals(elem)) {
	    		 metar.setStation( new String(ch, start, length) );
	    	 }	        
	    	
	    	 if ("iwxxm:airTemperature".equals(elem)) {
	    		 metar.setTemp((int)Float.parseFloat(new String(ch, start, length)) );
	    	 }	        
	
	    	 if ("iwxxm:dewpointTemperature".equals(elem)) {
	    		 metar.setDewPoint((int)Float.parseFloat(new String(ch, start, length)) );
	    	 }	        
	
	    	 if ("iwxxm:dewpointTemperature".equals(elem)) {
	    		 metar.setDewPoint((int)Float.parseFloat(new String(ch, start, length)) );
	    	 }	
	    	 
	    	 if ("iwxxm:qnh".equals(elem)) {
	    		 metar.setQnh((int)Float.parseFloat(new String(ch, start, length)) );
	    	 }	
	    	 
	    	 if ("iwxxm:meanWindDirection".equals(elem)) {
	    		 metar.setMeanWindDirection((int)Float.parseFloat(new String(ch, start, length)) );
	    	 }	
	    	 
	    	 if ("iwxxm:meanWindSpeed".equals(elem)) {
	    		 metar.setMeanWindSpeed((int)Float.parseFloat(new String(ch, start, length)) );
	    	 }	    	 
	    	 
	    	 if ("iwxxm:prevailingVisibility".equals(elem)) {
	    		 try {
	    		 	metar.setVisibility((int)Float.parseFloat(new String(ch, start, length)) );
				 }
				 catch( Exception e) {
					 System.err.println( "can't parse " + ch);
				 }
	    	 }	

	    	 if ("iwxxm:extremeClockwiseWindDirection".equals(elem)) {
	    		 metar.setWindVarMax((int)Float.parseFloat(new String(ch, start, length)) );
	    	 }	

	    	 if ("iwxxm:extremeCounterClockwiseWindDirection".equals(elem)) {
	    		 metar.setWindVarMin((int)Float.parseFloat(new String(ch, start, length)) );
	    	 }	
	    	 
	    	 if ("iwxxm:windGust".equals(elem)) {
	    		 metar.setGustWindSpeed((int)Float.parseFloat(new String(ch, start, length)) );
	    	 }	    	 
	    	 
	    	 if ("avi:input".equals(elem))   {
	    		 metar.setMessage(new String(ch, start, length));
	    	 }

			if ( cl != null && "iwxxm:base".equals(elem))   {
					cl.baseHeight = (int)Float.parseFloat(new String(ch, start, length));
			}
	    	 
	  /*      	String message = new String(ch, start, length);
	        	StringTokenizer st = new StringTokenizer( message, " ");
	        	String firstword = st.nextToken();
	        	String station = null;
	        	if(!firstword.equals("METAR")) {
	        		station=firstword;
	        	}
	        	else 
	        		station = st.nextToken();
	        	System.out.println("metar: " + station );
	        	
	        	String time = st.nextToken();
	        	String hours = time.substring(2, 4);
	        	String minutes = time.substring(4, 6);
	        	
	        	Metar m = new Metar();
	        	m.setTime(hours+minutes);
	        	m.setStation( station);
	        	
	        	String winds = st.nextToken();
	        	
	        	m.setMeanWindDirection(Integer.parseInt(winds.substring(0, 3)));
	           	m.setMeanWindSpeed(Integer.parseInt(winds.substring(3, 5)));
	   	        if( winds.contains("G")) {
	   	        	m.setGustWindSpeed(Integer.parseInt(winds.substring(7,9)));
	   	        }
	        	
	        	metarList.add(m);*/
	        
	    }
	 
	    /**
	     * Utility method for getting the current element in processing
	     * */
	    private String currentElement()
	    {
	        return (String)this.elementStack.peek();
	    }
	 
	    //Accessor for userList object
	    public ArrayList<Metar> getMetars()
	    {
	        return metarList;
	    }
}