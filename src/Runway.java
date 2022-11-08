public class Runway {
    public Runway(int code) {
        this.id = code;
    }

    public int getCode() {
        return id;
    }

    public void setCode(int code) {
        this.id = code;
    }

    private int id;

    @Override
    public boolean equals(Object code) {
        if (this == code) return true;
        if (code.getClass() == String.class)
            return this.id == id;
        else if(code.getClass() == Runway.class)
            return this.id == ((Runway) code).id;
        return false;
    }
}
