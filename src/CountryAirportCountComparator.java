import java.util.Comparator;

public class CountryAirportCountComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        Country c1 = (Country) o1;
        Country c2 = (Country) o2;
        if(c1.getAirportList().size()>c2.getAirportList().size())
            return -1;
        else if (c1.getAirportList().size()==c2.getAirportList().size())
            return 0;
        else
            return 1;
    }
}
