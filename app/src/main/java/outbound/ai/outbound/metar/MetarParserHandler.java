package outbound.ai.outbound.metar;

import java.util.ArrayList;
import java.util.Stack;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import outbound.ai.outbound.Metar;

public class MetarParserHandler  extends DefaultHandler{

	    //This is the list which shall be populated while parsing the XML.
	    private ArrayList<Metar> metarList = new ArrayList<Metar>();
	 
	    //As we read any XML element we will push that in this stack
	    private Stack elementStack = new Stack();
	 
	    //As we complete one user block in XML, we will push the User instance in userList
	    private Stack objectStack = new Stack();
	 
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

			if ("iwxxm:CloudLayer".equals(qName))   {
				try {
				String weatherTypeUri = attrs.getValue("xlink:href");
				String weatherCode = weatherTypeUri.substring(weatherTypeUri.lastIndexOf("/")+1,weatherTypeUri.length());
				metar.setPresentWeather(new String(weatherCode));
				}
				catch( Exception e) {
					System.err.println( "can't parse " + qName + ", attrs " + attrs );
				}
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
	            this.metarList.add(metar);
	            metar = null;
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