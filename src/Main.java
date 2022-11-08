import java.io.*;
import java.util.*;

public class Main {
    //path to files
    private static String COUNTRIES_PATH;
    private static String AIRPORTS_PATH;
    private static String RUNWAYS_PATH;

    //Following variables represent the field name to obtain the data
    private static final String COUNTRY_CODE_COLUMN = "code";
    private static final String COUNTRY_NAME_COLUMN = "name";
    private static final String AIRPORT_CODE_COLUMN = "ident";
    private static final String AIRPORT_COUNTRY_CODE_COLUMN = "iso_country";
    private static final String RUNWAY_CODE_COLUMN = "id";
    private static final String RUNWAY_AIRPORT_CODE_COLUMN = "airport_ident";

    //Change to see more or less airports in the ranking
    private static final int NUMBER_OF_RESULTS = 10;


    public static void  main(String[] args) {
        //Parameter check
        if(args.length < 4){
            System.out.println("ERROR: Number of arguments is incorrect. " +
                    "You must provide a partial/full code or name of a country" +
                    "And the three csv files. Example\n" +
                    "java -jar airportAssessment.jar countries.csv airports.csv runways.csv" );
            return ;
        }
        COUNTRIES_PATH=args[1];
        AIRPORTS_PATH=args[2];
        RUNWAYS_PATH=args[3];
        //First retrieve countries data (Only code and name)
        HashMap<String,Country> countries = readCountriesFromFile();
        //Then retrieve runway data and export it to a hashmap so we can easily access later
        HashMap<String, Airport> airports = fillAirports();
        //Retrieve airport data to fill the airport details (including which country are they on)
        fillCountries(airports,countries);
        //Now we have all the data represented in Objects

        //First we print the countries with most airports
        printCountriesWithMostAirports(countries);
        //Then we filter the runways by country and airports.
        printRunwaysOfCountry(pairCountry(countries,args[0]));
    }

    private static void printRunwaysOfCountry(Country country){
        System.out.println("Country specified:\t"+country.getName());
        for (Airport airport : country.getAirportList().stream().filter(x -> !x.getRunwayList().isEmpty()).toList()){
            System.out.println("\tAirport:\t"+airport.getCode());
            for (Runway runway : airport.getRunwayList()){
                System.out.println("\t\tRunway:\t"+runway.getCode());
            }
        }
    }
    private static void printCountriesWithMostAirports(HashMap<String,Country> countries){
        //Retrieves an ordered list of the countries with most  airports limited to the number specified
        List<Country> top = countries
                .values()
                .stream()
                //Custom comparator to order the stream on a descending way
                .sorted(new CountryAirportCountComparator())
                .limit(NUMBER_OF_RESULTS).toList();
        for (Country country : top)
            System.out.println(country.getName() + "\t|\tnumber of airports: " + country.getAirportList().size());
    }

    //Reads the countries.csv file and translates it into Java Objects
    private static HashMap<String,Country> readCountriesFromFile(){
        try {
            HashMap<String,Country> countries = new HashMap<>();
            BufferedReader br;
            br = new BufferedReader(new FileReader(COUNTRIES_PATH));
            String line = br.readLine();
            String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            int countryCodeIndex = getColumnIndex(columns, COUNTRY_CODE_COLUMN);
            int countryNameIndex = getColumnIndex(columns, COUNTRY_NAME_COLUMN);
            while((line = br.readLine()) != null){
                //Splits only by commas if they are not between " "
                String[] splitLine = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)",4);
                //the values have commas so we remove them and normalize them by uppercasing them
                String countryCode = splitLine[countryCodeIndex].replaceAll("\"","").toUpperCase();
                String countryName = splitLine[countryNameIndex].replaceAll("\"","").toUpperCase();
                countries.put(countryCode,new Country(countryCode,countryName));
            }
            return countries;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Reads the airports.csv file and translates it into Java Objects
    private static void fillCountries(HashMap<String,Airport> airports,HashMap<String,Country> countries){
        try {
            BufferedReader br = new BufferedReader(new FileReader(AIRPORTS_PATH));
            String line = br.readLine();
            String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            int airportCodeIndex = getColumnIndex(columns, AIRPORT_CODE_COLUMN);
            int countryCodeIndex = getColumnIndex(columns, AIRPORT_COUNTRY_CODE_COLUMN);
            while((line = br.readLine()) != null){
                String[] splitLine = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                String countryCode = splitLine[countryCodeIndex].replaceAll("\"","");
                String airportCode = splitLine[airportCodeIndex].replaceAll("\"","");
                //If the airport wasn't created (because there was no runway related with it) it creates it
                Airport airport = airports.computeIfAbsent(airportCode,ignore -> new Airport(airportCode));
                countries.get(countryCode).getAirportList().add(airport);
            }
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Reads the runways.csv file and translates it into Java Objects
    private static HashMap<String,Airport> fillAirports(){
        try {
            HashMap<String,Airport> airports = new HashMap<>();
            BufferedReader br = new BufferedReader(new FileReader(RUNWAYS_PATH));
            String line = br.readLine();
            String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            int runwayCodeIndex = getColumnIndex(columns, RUNWAY_CODE_COLUMN);
            int airportCodeIndex = getColumnIndex(columns, RUNWAY_AIRPORT_CODE_COLUMN);
            while((line = br.readLine()) != null){
                String[] splitLine = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                Runway runway = new Runway(Integer.parseInt(splitLine[runwayCodeIndex]));
                String airportCode = splitLine[airportCodeIndex].replaceAll("\"","");
                //Since we have a 1 airport:N runways relationship we need to check if the airport was already created
                airports.computeIfAbsent(airportCode,ignored -> new Airport(airportCode)).getRunwayList().add(runway);
            }
            return airports;
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //gets the field index of the field name specified in a csv file
    private static int getColumnIndex(String[] columns, String column){
        for(int i = 0; i < columns.length ; i++)
            if(columns[i].replace("\"","").equals(column))
                return i;
        return -1;
    }

    //Checks if there is an exact match of the name/code specified. If not, it tries a fuzzy search
    private static Country pairCountry(HashMap<String,Country> countries,String string){
        Optional<Country> country = countries.values().stream().filter(x -> x.isRepresentedBy(string.toUpperCase())).findFirst();
        return country.orElseGet(()-> fuzzySearch(countries,string));
    }

    //Looks for the best fuzzy Score in the countries names and codes for the string specified
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Country fuzzySearch(HashMap<String,Country> countries, String string){
        //retrieves the country where the name had the maximum fuzzy score
        Country maxFuzzyScoreByCountryName = countries
                .values()
                .stream()
                .max(Comparator.comparingInt(country -> fuzzyScore(country.getName(),string.toUpperCase())))
                .get();
        //retrieves the country where the code had the maximum fuzzy score
        Country maxFuzzyScoreByCountryCode =countries
                .values()
                .stream()
                .max(Comparator.comparingInt(country -> fuzzyScore(country.getCode(),string.toUpperCase())))
                .get();
        //returns the max from the 2 prioritizing the name in case they are equal
        return (fuzzyScore(maxFuzzyScoreByCountryName.getName(),string.toUpperCase()) >=
                    fuzzyScore(maxFuzzyScoreByCountryCode.getCode(),string.toUpperCase())) ?
                    maxFuzzyScoreByCountryName :
                    maxFuzzyScoreByCountryCode;
    }

    //Found this function to calculate fuzzyScore
    public static Integer fuzzyScore(String term, String query) {
        if (term != null && query != null) {
            String termLowerCase = term.toLowerCase(Locale.ENGLISH);
            String queryLowerCase = query.toLowerCase(Locale.ENGLISH);
            int score = 0;
            int termIndex = 0;
            int previousMatchingCharacterIndex = Integer.MIN_VALUE;

            for(int queryIndex = 0; queryIndex < queryLowerCase.length(); ++queryIndex) {
                char queryChar = queryLowerCase.charAt(queryIndex);

                for(boolean termCharacterMatchFound = false; termIndex < termLowerCase.length() && !termCharacterMatchFound; ++termIndex) {
                    char termChar = termLowerCase.charAt(termIndex);
                    if (queryChar == termChar) {
                        ++score;
                        if (previousMatchingCharacterIndex + 1 == termIndex) {
                            score += 2;
                        }

                        previousMatchingCharacterIndex = termIndex;
                        termCharacterMatchFound = true;
                    }
                }
            }
            return score;
        } else {
            throw new IllegalArgumentException("Strings must not be null");
        }
    }
}