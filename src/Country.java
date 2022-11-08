import java.util.ArrayList;
import java.util.List;

public class Country {
    private String code;
    private String name;
    private List<Airport> airportList = new ArrayList<>();

    public List<Airport> getAirportList() {
        return airportList;
    }

    public void setAirportList(List<Airport> airportList) {
        this.airportList = airportList;
    }
    public Country(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRepresentedBy(String codeOrName){
        return codeOrName.equals(code) || codeOrName.equals(name);
    }

}
