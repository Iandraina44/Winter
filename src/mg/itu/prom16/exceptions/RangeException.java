package mg.itu.prom16.exceptions;

import mg.itu.prom16.annotations.Range;

public class RangeException extends Exception {
    public RangeException(String paramName,Range range) {
        super("Le parametre " + paramName + " doit etre dans la plage [" + range.min()
        + "," + range.max() + "]");
    }
}
