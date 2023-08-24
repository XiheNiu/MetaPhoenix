package net.csibio.metaphoenix.client.domain.bean.math;

import lombok.Data;

@Data
public class SlopeIntercept {

    Double slope;

    Double intercept;

    public SlopeIntercept() {
    }

    public SlopeIntercept(double slope, double intercept) {
        this.slope = slope;
        this.intercept = intercept;
    }

    public static SlopeIntercept create() {
        SlopeIntercept si = new SlopeIntercept();
        si.setIntercept(0d);
        si.setSlope(0d);
        return si;
    }

    public double realRt(double libRt) {
        return (libRt - intercept) / slope;
    }

    @Override
    public String toString() {
        return "Slope:" + slope + ";Intercept:" + intercept;
    }

    public String getFormula() {
        return "y=" + String.format("%.3f", slope) + "x" + (intercept > 0 ? ("+" + String.format("%.3f", intercept)) : String.format("%.3f", intercept));
    }
}
