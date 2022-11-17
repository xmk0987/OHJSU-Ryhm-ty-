package fi.tuni.roadwatch;
import com.sothawo.mapjfx.Coordinate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WeatherAPILogic {
    private double currentTemp;
    private double currentWind;
    private double currentCloud;


    private Date dateAndTime = Calendar.getInstance().getTime();

    private final ArrayList<WeatherData> weatherpast12 = new ArrayList<>();


    // Changes date in to string 8601Format to use in urlstring
    public String timeAndDateToIso8601Format(Date date){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .format(date);
    }

    // Changes date String in to string 8601Format to use in urlstring
    public Date timeAndDateAsDate(String datestring) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(datestring);
    }

    // Creates URL String based on given parameters to be used in creating API document
    public String createURLString(Double latitude, Double longitude, String startime, String endtime) throws ParseException {
        String coordinates = latitude.toString() + "," + longitude.toString();
        StringBuilder str = new StringBuilder();

        // Compares given starttime date to current time to see if forecast or observation
        if(timeAndDateAsDate(startime).after(dateAndTime)){
            str.append("https://opendata.fmi.fi/wfs?request=getFeature&version=2.0.0&storedquery_id=fmi::forecast::harmonie::surface::point::simple&latlon=").append(coordinates)
                    .append("&timestep=10&starttime=").append(startime).append("&endtime=").append(endtime).append("&parameters=temperature,windspeedms");
        }
        else{
            double longitude2 = longitude +1;
            double latitude2 = latitude+1;
            String coordinateBbox = longitude + "," + latitude + "," + longitude2 + "," + latitude2;


            str.append("https://opendata.fmi.fi/wfs?request=getFeature&version=2.0.0&storedquery_id=fmi::observations::weather::simple&bbox=").append(coordinateBbox)
                    .append("&starttime=").append(startime).append("&endtime=").append(endtime).append("&timestep=120&parameters=t2m,ws_10min,n_man");
        }

        return str.toString();

    }

    // Creates Document element based on given url. Used in creadingWeatherData
    public Document GetApiDocument(String url) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new URL(url).openStream());
        doc.getDocumentElement().normalize();

        return doc;
    }

    // Creates observations weather data. Has to be different function due to different parameter names versus forecast
    public ArrayList<WeatherData> creatingWeatherObservations(Document doc) throws ParseException {
        NodeList nList = doc.getElementsByTagName("wfs:member");
        int counter = 0;
        for (int temp = 0; temp < nList.getLength(); temp++) {
            counter ++;
            Node nNode = nList.item(temp);
            Element eElement = (Element) nNode;
            String currentTime = eElement.getElementsByTagName("BsWfs:Time")
                    .item(0).getTextContent();
            Date currentDate = timeAndDateAsDate(currentTime);

            String currentCoordinates = eElement.getElementsByTagName("gml:pos")
                    .item(0).getTextContent();


            if(nNode.getNodeType() == Node.ELEMENT_NODE){
                if (currentTime.equals(eElement.getElementsByTagName("BsWfs:Time")
                        .item(0).getTextContent()) ){

                    String paramName = eElement.getElementsByTagName("BsWfs:ParameterName")
                            .item(0).getTextContent();
                    if( paramName.equals("t2m") ){
                        this.currentTemp = Double.parseDouble(eElement.getElementsByTagName("BsWfs:ParameterValue")
                                .item(0).getTextContent());
                    }
                    if( paramName.equals("ws_10min") ){
                        this.currentWind = Double.parseDouble(eElement.getElementsByTagName("BsWfs:ParameterValue")
                                .item(0).getTextContent());
                    }
                    if( paramName.equals("n_man") ){
                        this.currentCloud = Double.parseDouble(eElement.getElementsByTagName("BsWfs:ParameterValue")
                                .item(0).getTextContent());
                    }

                    // Saves all weatherdata members to arraylist of weatherdata
                    if(counter % 3 == 0){
                        WeatherData savetemp = new WeatherData(currentTemp, currentWind, currentCloud,currentDate , currentCoordinates);
                        if(!weatherpast12.contains(savetemp)){
                            weatherpast12.add(savetemp);
                        }
                    }


                }

            }
        }
        return weatherpast12;
    }

    // Creates forecast weather data. Has to be different function due to different parameter names versus observations
    public ArrayList<WeatherData> creatingWeatherForecast(Document doc) throws ParseException {
        NodeList nList = doc.getElementsByTagName("wfs:member");
        int counter = 0;
        for (int temp = 0; temp < nList.getLength(); temp++) {
            counter ++;
            Node nNode = nList.item(temp);
            Element eElement = (Element) nNode;
            String currentTime = eElement.getElementsByTagName("BsWfs:Time")
                    .item(0).getTextContent();
            Date currentDate = timeAndDateAsDate(currentTime);

            String currentCoordinates = eElement.getElementsByTagName("gml:pos")
                    .item(0).getTextContent();


            if(nNode.getNodeType() == Node.ELEMENT_NODE){
                if (currentTime.equals(eElement.getElementsByTagName("BsWfs:Time")
                        .item(0).getTextContent()) ){

                    String paramName = eElement.getElementsByTagName("BsWfs:ParameterName")
                            .item(0).getTextContent();
                    if( paramName.equals("temperature") ){
                        this.currentTemp = Double.parseDouble(eElement.getElementsByTagName("BsWfs:ParameterValue")
                                .item(0).getTextContent());
                    }
                    if( paramName.equals("windspeedms") ){
                        this.currentWind = Double.parseDouble(eElement.getElementsByTagName("BsWfs:ParameterValue")
                                .item(0).getTextContent());
                    }

                    if(counter % 2 == 0){
                        WeatherData savetemp = new WeatherData(currentTemp, currentWind, currentCloud,currentDate , currentCoordinates);
                        if(!weatherpast12.contains(savetemp)){
                            weatherpast12.add(savetemp);
                        }
                    }


                }

            }
        }
        return weatherpast12;
    }





}
