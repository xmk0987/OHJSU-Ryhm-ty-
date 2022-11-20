package fi.tuni.roadwatch;

import com.sothawo.mapjfx.Coordinate;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class SessionData {

//    private RoadWatchController roadWatchController;
//    private MapController mapController;

    public Coordinate currentCoordinates;
    public List<Coordinate> polyCoordinates = new ArrayList<>();
    private Date dateAndTime = Calendar.getInstance().getTime();

    public ArrayList<WeatherData> WantedWeatherData = new ArrayList<>();
    public ArrayList<WeatherDataMinMaxAvg> wantedWeatherAVGMinMax = new ArrayList<>();

    public TrafficMessage trafficMessage = new TrafficMessage();

    // Used in creation of wantedWeatherData
    private double currentTemp;
    private double currentWind;
    private double currentCloud;

    public static class CoordinateConstraints{

        public CoordinateConstraints(Double minLon, Double minLat, Double maxLon, Double maxLat){
            this.minLon = minLon;
            this.minLat = minLat;

            this.maxLon = maxLon;
            this.maxLat = maxLat;

        }
        private final Double minLon;
        private final Double minLat;

        private final Double maxLon;
        private final Double maxLat;

        public String getAsString(Character c){
            return ""+minLon + c + minLat + c + maxLon + c + maxLat;
        }
    }

    public CoordinateConstraints coordinateConstraints;

    public static RoadAPILogic roadAPILogic;
    public static WeatherAPILogic weatherAPILogic;


    public SessionData() throws URISyntaxException, IOException {
        roadAPILogic = new RoadAPILogic();
        weatherAPILogic = new WeatherAPILogic();
        trafficMessage = roadAPILogic.getTrafficMessages();

    }

    public void setCurrentCoordinates(Coordinate newCoordinate){
        currentCoordinates = newCoordinate;
    }

    public void setPolygonCoordinates(List<Coordinate> polyCoordinates){
        this.polyCoordinates.addAll(polyCoordinates);
    }

    public void calculateMinMaxCoordinates() {

        // TODO: Make more efficient
        if(polyCoordinates != null){
            if(!polyCoordinates.isEmpty()){
                Double maxLongtitude = polyCoordinates.stream().max(Comparator.comparing(Coordinate::getLongitude)).get().getLongitude();
                Double maxLatitude = polyCoordinates.stream().max(Comparator.comparing(Coordinate::getLatitude)).get().getLatitude();

                Double minLongtitude = polyCoordinates.stream().min(Comparator.comparing(Coordinate::getLongitude)).get().getLongitude();
                Double minLatitude = polyCoordinates.stream().min(Comparator.comparing(Coordinate::getLatitude)).get().getLatitude();

                coordinateConstraints = new CoordinateConstraints(minLongtitude, minLatitude, maxLongtitude, maxLatitude);
                System.out.println(coordinateConstraints.getAsString('/'));
            }
            // TODO: ADD THIS TO ROADDATA CONSTRUCTOR
            // TODO: Make nicer maybe
            // Checks the traffic messages in a given area
            ArrayList<TrafficMessage.Feature> messagesInArea = new ArrayList<>();
            for (TrafficMessage.Feature feature : trafficMessage.features){
                if(feature.geometry != null){
                    for (ArrayList<ArrayList<Double>> coordinates : feature.geometry.coordinates) {
                        for (ArrayList<Double> coordinate : coordinates) {
                            if(coordinate.size() == 2){
                                if(coordinate.get(0) > coordinateConstraints.minLon &&
                                        coordinate.get(0) < coordinateConstraints.maxLon &&
                                        coordinate.get(1) > coordinateConstraints.minLat &&
                                        coordinate.get(1) < coordinateConstraints.maxLat){
                                    messagesInArea.add(feature);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
            // TEST PRINTS
            System.out.println(messagesInArea.size() + " Traffic messages in the area");
//            for (TrafficMessage.Feature feature : messagesInArea){
//                if(feature.properties.announcements != null) {
//                    for(TrafficMessage.Announcements announcement: feature.properties.announcements){
//                        System.out.println(announcement.title);
//                    }
//                }
//            }
        }

    }

    public void createAvgMinMax(Date startTime, Date endTime) throws ParseException, ParserConfigurationException, IOException, SAXException {
        // Creates the URL String to be used according to parameters wanted that include coordinates and start and end time
        // than creates the document used to create the arraylist of WeatherData
        String startTimeString = weatherAPILogic.timeAndDateToIso8601Format(startTime);
        String endTimeString = weatherAPILogic.timeAndDateToIso8601Format(endTime);
        String urlstring = weatherAPILogic.createAVGMINMAXurlString(coordinateConstraints.getAsString(','),  startTimeString, endTimeString);
        System.out.println(urlstring);
        this.wantedWeatherAVGMinMax = weatherAPILogic.creatingAvgMinMax(weatherAPILogic.GetApiDocument(urlstring));

    }

    // WeatherData creation to sessionData
    public void createWeatherData(Date startTime, Date endTime) throws ParserConfigurationException, IOException, SAXException, ParseException {
        // Creates the URL String to be used according to parameters wanted that include coordinates and start and end time
        // than creates the document used to create the arraylist of WeatherData
        String startTimeString = weatherAPILogic.timeAndDateToIso8601Format(startTime);
        String endTimeString = weatherAPILogic.timeAndDateToIso8601Format(endTime);
        String urlstring = weatherAPILogic.createURLString(currentCoordinates.getLatitude(),currentCoordinates.getLongitude(),  startTimeString, endTimeString);

       //Test to see what api is found with parameters
        System.out.println(urlstring);
        // Compares current date to starTime to know if we want to create a weatherforecast or weather
        // observation
        if(startTime.after(dateAndTime)){
            this.WantedWeatherData = weatherAPILogic.creatingWeatherForecast(weatherAPILogic.GetApiDocument(urlstring));
        }
        this.WantedWeatherData = weatherAPILogic.creatingWeatherObservations(weatherAPILogic.GetApiDocument(urlstring));

    }

    // Helper function to get the closest date to current
    public Date getClosestDate(){
        ArrayList<Date> alldates = new ArrayList<>();
        for (WeatherData wd : this.WantedWeatherData){
            alldates.add(wd.getDate());
        }

        Date closest = Collections.min(alldates, new Comparator<Date>() {
            @Override
            public int compare(Date o1, Date o2) {
                long diff1 = Math.abs(o1.getTime() - dateAndTime.getTime());
                long diff2 = Math.abs(o2.getTime() - dateAndTime.getTime());
                return diff1 < diff2 ? -1:1;
            }
        });
        return closest;
    }

    public double getMIN_value(){
        double min = wantedWeatherAVGMinMax.get(0).getTempMIN();

        for(WeatherDataMinMaxAvg wd : wantedWeatherAVGMinMax){
            if(wd.getTempMIN() <= min){
                min = wd.getTempMIN();
            }

        }

        return min;
    }

    public double getMAX_value(){
        double max = wantedWeatherAVGMinMax.get(0).getTempMAX();

        for(WeatherDataMinMaxAvg wd : wantedWeatherAVGMinMax){
            if(wd.getTempMAX() >= max){
                max = wd.getTempMAX();
            }
        }

        return max;
    }

    public String getAVG_value(){
        double average = wantedWeatherAVGMinMax.get(0).getTempAverage();;
        for(WeatherDataMinMaxAvg wd : wantedWeatherAVGMinMax){
            average += wd.getTempAverage();
        }

        DecimalFormat df = new DecimalFormat("0.00");
        average = average/wantedWeatherAVGMinMax.size();

        return df.format(average);
    }





}
