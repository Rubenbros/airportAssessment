import java.util.ArrayList;
import java.util.List;

public class Airport {
    private String code;
    private List<Runway> runwayList = new ArrayList<>();

    public Airport(String code) {
        this.code = code;
    }

    public List<Runway> getRunwayList() {
        return runwayList;
    }

    public void setRunwayList(List<Runway> runwayList) {
        this.runwayList = runwayList;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object code) {
        if (this == code) return true;
        if (code.getClass() == String.class)
            return this.code == code;
        else if(code.getClass() == Airport.class)
            return this.code == ((Airport) code).code;
        return false;
    }
}
