package uk.ac.bris.cs.spe.navigationaltool;

import java.awt.*;

public class LocationDataHolder {
    public int id = 0;
    public int x = 0;
    public int y = 0;
    public String code = "";
    public String name = "";
    public String floor = "";


    LocationDataHolder(int idd, int xx, int yy, String floor, String code, String name) {
        id = idd;
        x = xx;
        y = yy;
        this.floor = floor;
        this.code = code;
        this.name = name;
    }

    String getString() {
        return code.equals("") ? ("(" + x + "," + y + ")") : code;
    }
}
