package id.duglegir.jagosholat.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;


public class WaktuShalatHelper {

    private int calcMethod;
    private int asrJuristic;
    private int dhuhrMinutes;
    private int adjustHighLats;
    private int timeFormat;
    private double lat;
    private double lng;
    private double timeZone;
    private double JDate;


    public int Jafari;
    public int Karachi;
    public int ISNA;
    public int MWL;
    public int Makkah;
    public int Egypt;
    public int Custom;
    public int Tehran;

    public int Shafii;
    public int Hanafi;

    public int None;
    public int MidNight;
    public int OneSeventh;
    public int AngleBased;

    public int Time24;
    public int Time12;
    public int Time12NS;
    public int Floating;

    private ArrayList<String> timeNames;
    private String InvalidTime;

    private int numIterations;

    private HashMap<Integer, double[]> methodParams;

    private double[] prayerTimesCurrent;
    private int[] offsets;

    public WaktuShalatHelper(){
        this.setCalcMethod(0);
        this.setAsrJuristic(0);
        this.setDhuhrMinutes(0);
        this.setAdjustHighLats(1);
        this.setTimeFormat(0);

        this.setJafari(0);
        this.setKarachi(1);
        this.setISNA(2);
        this.setMWL(3);
        this.setMakkah(4);
        this.setEgypt(5);
        this.setTehran(6);
        this.setCustom(7);

        this.setShafii(0);
        this.setHanafi(1);

        this.setNone(0);
        this.setMidNight(1);
        this.setOneSeventh(2);
        this.setAngleBased(3);

        this.setTime24(0);
        this.setTime12(1);
        this.setTime12NS(2);
        this.setFloating(3);

        timeNames = new ArrayList<String>();
        timeNames.add("Shubuh");
        timeNames.add("Matahari Terbit");
        timeNames.add("Dzuhur");
        timeNames.add("Ashar");
        timeNames.add("Matahari Terbenam");
        timeNames.add("Maghrib");
        timeNames.add("Isya");

        InvalidTime = "-----";


        this.setNumIterations(1);



        offsets = new int[7];
        offsets[0] = 0;
        offsets[1] = 0;
        offsets[2] = 0;
        offsets[3] = 0;
        offsets[4] = 0;
        offsets[5] = 0;
        offsets[6] = 0;

        methodParams = new HashMap<Integer, double[]>();

        double[] Jvalues = { 16, 0, 4, 0, 14 };
        methodParams.put(Integer.valueOf(this.getJafari()), Jvalues);

        double[] Kvalues = { 18, 1, 0, 0, 18 };
        methodParams.put(Integer.valueOf(this.getKarachi()), Kvalues);

        double[] Ivalues = { 15, 1, 0, 0, 15 };
        methodParams.put(Integer.valueOf(this.getISNA()), Ivalues);

        double[] MWvalues = { 18, 1, 0, 0, 17 };
        methodParams.put(Integer.valueOf(this.getMWL()), MWvalues);

        double[] MKvalues = { 18.5, 1, 0, 1, 90 };
        methodParams.put(Integer.valueOf(this.getMakkah()), MKvalues);

        double[] Evalues = { 19.5, 1, 0, 0, 17.5 };
        methodParams.put(Integer.valueOf(this.getEgypt()), Evalues);

        double[] Tvalues = { 17.7, 0, 4.5, 0, 14 };
        methodParams.put(Integer.valueOf(this.getTehran()), Tvalues);

        double[] Cvalues = { 18, 1, 0, 0, 17 };
        methodParams.put(Integer.valueOf(this.getCustom()), Cvalues);
    }


    private double fixangle(double a) {

        a = a - (360 * (Math.floor(a / 360.0)));

        a = a < 0 ? (a + 360) : a;

        return a;
    }

    private double fixhour(double a) {
        a = a - 24.0 * Math.floor(a / 24.0);
        a = a < 0 ? (a + 24) : a;
        return a;
    }

    private double radiansToDegrees(double alpha) {
        return ((alpha * 180.0) / Math.PI);
    }

    private double DegreesToRadians(double alpha) {
        return ((alpha * Math.PI) / 180.0);
    }

    private double dsin(double d) {
        return (Math.sin(DegreesToRadians(d)));
    }

    private double dcos(double d) {
        return (Math.cos(DegreesToRadians(d)));
    }

    private double dtan(double d) {
        return (Math.tan(DegreesToRadians(d)));
    }

    private double darcsin(double x) {
        double val = Math.asin(x);
        return radiansToDegrees(val);
    }

    private double darccos(double x) {
        double val = Math.acos(x);
        return radiansToDegrees(val);
    }

    private double darctan(double x) {
        double val = Math.atan(x);
        return radiansToDegrees(val);
    }

    private double darctan2(double y, double x) {
        double val = Math.atan2(y, x);
        return radiansToDegrees(val);
    }

    private double darccot(double x) {
        double val = Math.atan2(1.0, x);
        return radiansToDegrees(val);
    }


    private double getTimeZone1() {
        TimeZone timez = TimeZone.getDefault();
        double hoursDiff = (timez.getRawOffset() / 1000.0) / 3600;
        return hoursDiff;
    }

    private double getBaseTimeZone() {
        TimeZone timez = TimeZone.getDefault();
        double hoursDiff = (timez.getRawOffset() / 1000.0) / 3600;
        return hoursDiff;

    }

    private double detectDaylightSaving() {
        TimeZone timez = TimeZone.getDefault();
        double hoursDiff = timez.getDSTSavings();
        return hoursDiff;
    }


    private double julianDate(int year, int month, int day) {

        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double A = Math.floor(year / 100.0);

        double B = 2 - A + Math.floor(A / 4.0);

        double JD = Math.floor(365.25 * (year + 4716))
                + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;

        return JD;
    }

    private double calcJD(int year, int month, int day) {
        double J1970 = 2440588.0;
        Date date = new Date(year, month - 1, day);

        double ms = date.getTime();

        double days = Math.floor(ms / (1000.0 * 60.0 * 60.0 * 24.0));
        return J1970 + days - 0.5;

    }





    private double[] sunPosition(double jd) {

        double D = jd - 2451545;
        double g = fixangle(357.529 + 0.98560028 * D);
        double q = fixangle(280.459 + 0.98564736 * D);
        double L = fixangle(q + (1.915 * dsin(g)) + (0.020 * dsin(2 * g)));


        double e = 23.439 - (0.00000036 * D);
        double d = darcsin(dsin(e) * dsin(L));
        double RA = (darctan2((dcos(e) * dsin(L)), (dcos(L)))) / 15.0;
        RA = fixhour(RA);
        double EqT = q / 15.0 - RA;
        double[] sPosition = new double[2];
        sPosition[0] = d;
        sPosition[1] = EqT;

        return sPosition;
    }

    private double equationOfTime(double jd) {
        double eq = sunPosition(jd)[1];
        return eq;
    }

    private double sunDeclination(double jd) {
        double d = sunPosition(jd)[0];
        return d;
    }

    private double computeMidDay(double t) {
        double T = equationOfTime(this.getJDate() + t);
        double Z = fixhour(12 - T);
        return Z;
    }

    private double computeTime(double G, double t) {

        double D = sunDeclination(this.getJDate() + t);
        double Z = computeMidDay(t);
        double Beg = -dsin(G) - dsin(D) * dsin(this.getLat());
        double Mid = dcos(D) * dcos(this.getLat());
        double V = darccos(Beg / Mid) / 15.0;

        return Z + (G > 90 ? -V : V);
    }


    private double computeAsr(double step, double t) {
        double D = sunDeclination(this.getJDate() + t);
        double G = -darccot(step + dtan(Math.abs(this.getLat() - D)));
        return computeTime(G, t);
    }


    private double timeDiff(double time1, double time2) {
        return fixhour(time2 - time1);
    }


    private ArrayList<String> getDatePrayerTimes(int year, int month, int day,
                                                 double latitude, double longitude, double tZone) {
        this.setLat(latitude);
        this.setLng(longitude);
        this.setTimeZone(tZone);
        this.setJDate(julianDate(year, month, day));
        double lonDiff = longitude / (15.0 * 24.0);
        this.setJDate(this.getJDate() - lonDiff);
        return computeDayTimes();
    }

    public ArrayList<String> getPrayerTimes(Calendar date, double latitude,
                                            double longitude, double tZone) {

        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DATE);

        return getDatePrayerTimes(year, month + 1, day, latitude, longitude,
                tZone);
    }

    private void setCustomParams(double[] params) {

        for (int i = 0; i < 5; i++) {
            if (params[i] == -1) {
                params[i] = methodParams.get(this.getCalcMethod())[i];
                methodParams.put(this.getCustom(), params);
            } else {
                methodParams.get(this.getCustom())[i] = params[i];
            }
        }
        this.setCalcMethod(this.getCustom());
    }

    public void setFajrAngle(double angle) {
        double[] params = { angle, -1, -1, -1, -1 };
        setCustomParams(params);
    }

    public void setMaghribAngle(double angle) {
        double[] params = { -1, 0, angle, -1, -1 };
        setCustomParams(params);

    }

    public void setIshaAngle(double angle) {
        double[] params = { -1, -1, -1, 0, angle };
        setCustomParams(params);

    }

    public void setMaghribMinutes(double minutes) {
        double[] params = { -1, 1, minutes, -1, -1 };
        setCustomParams(params);

    }

    public void setIshaMinutes(double minutes) {
        double[] params = { -1, -1, -1, 1, minutes };
        setCustomParams(params);

    }

    public String floatToTime24(double time) {

        String result;

        if (Double.isNaN(time)) {
            return InvalidTime;
        }

        time = fixhour(time + 0.5 / 60.0);
        int hours = (int) Math.floor(time);
        double minutes = Math.floor((time - hours) * 60.0);

        if ((hours >= 0 && hours <= 9) && (minutes >= 0 && minutes <= 9)) {
            result = "0" + hours + " : 0" + Math.round(minutes);
        } else if ((hours >= 0 && hours <= 9)) {
            result = "0" + hours + " : " + Math.round(minutes);
        } else if ((minutes >= 0 && minutes <= 9)) {
            result = hours + " : 0" + Math.round(minutes);
        } else {
            result = hours + " : " + Math.round(minutes);
        }
        return result;
    }

    public String floatToTime12(double time, boolean noSuffix) {

        if (Double.isNaN(time)) {
            return InvalidTime;
        }

        time = fixhour(time + 0.5 / 60);
        int hours = (int) Math.floor(time);
        double minutes = Math.floor((time - hours) * 60);
        String suffix, result;
        if (hours >= 12) {
            suffix = "pm";
        } else {
            suffix = "am";
        }
        hours = ((((hours + 12) - 1) % (12)) + 1);
        
        if (noSuffix == false) {
            if ((hours >= 0 && hours <= 9) && (minutes >= 0 && minutes <= 9)) {
                result = "0" + hours + " : 0" + Math.round(minutes) + " "
                        + suffix;
            } else if ((hours >= 0 && hours <= 9)) {
                result = "0" + hours + " : " + Math.round(minutes) + " " + suffix;
            } else if ((minutes >= 0 && minutes <= 9)) {
                result = hours + " : 0" + Math.round(minutes) + " " + suffix;
            } else {
                result = hours + " : " + Math.round(minutes) + " " + suffix;
            }

        } else {
            if ((hours >= 0 && hours <= 9) && (minutes >= 0 && minutes <= 9)) {
                result = "0" + hours + " : 0" + Math.round(minutes);
            } else if ((hours >= 0 && hours <= 9)) {
                result = "0" + hours + " : " + Math.round(minutes);
            } else if ((minutes >= 0 && minutes <= 9)) {
                result = hours + " : 0" + Math.round(minutes);
            } else {
                result = hours + " : " + Math.round(minutes);
            }
        }
        return result;

    }

    public String floatToTime12NS(double time) {
        return floatToTime12(time, true);
    }


    private double[] computeTimes(double[] times) {

        double[] t = dayPortion(times);

        double Fajr = this.computeTime(
                180 - methodParams.get(this.getCalcMethod())[0], t[0]);

        double Sunrise = this.computeTime(180 - 0.833, t[1]);

        double Dhuhr = this.computeMidDay(t[2]);
        double Asr = this.computeAsr(1 + this.getAsrJuristic(), t[3]);
        double Sunset = this.computeTime(0.833, t[4]);

        double Maghrib = this.computeTime(
                methodParams.get(this.getCalcMethod())[2], t[5]);
        double Isha = this.computeTime(
                methodParams.get(this.getCalcMethod())[4], t[6]);

        double[] CTimes = { Fajr, Sunrise, Dhuhr, Asr, Sunset, Maghrib, Isha };

        return CTimes;

    }

    private ArrayList<String> computeDayTimes() {
        double[] times = { 5, 6, 12, 13, 18, 18, 18 };

        for (int i = 1; i <= this.getNumIterations(); i++) {
            times = computeTimes(times);
        }

        times = adjustTimes(times);
        times = tuneTimes(times);

        return adjustTimesFormat(times);
    }

    private double[] adjustTimes(double[] times) {
        for (int i = 0; i < times.length; i++) {
            times[i] += this.getTimeZone() - this.getLng() / 15;
        }

        times[2] += this.getDhuhrMinutes() / 60;
        if (methodParams.get(this.getCalcMethod())[1] == 1)
        {
            times[5] = times[4] + methodParams.get(this.getCalcMethod())[2]
                    / 60;
        }
        if (methodParams.get(this.getCalcMethod())[3] == 1)
        {
            times[6] = times[5] + methodParams.get(this.getCalcMethod())[4]
                    / 60;
        }

        if (this.getAdjustHighLats() != this.getNone()) {
            times = adjustHighLatTimes(times);
        }

        return times;
    }

    private ArrayList<String> adjustTimesFormat(double[] times) {

        ArrayList<String> result = new ArrayList<String>();

        if (this.getTimeFormat() == this.getFloating()) {
            for (double time : times) {
                result.add(String.valueOf(time));
            }
            return result;
        }

        for (int i = 0; i < 7; i++) {
            if (this.getTimeFormat() == this.getTime12()) {
                result.add(floatToTime12(times[i], false));
            } else if (this.getTimeFormat() == this.getTime12NS()) {
                result.add(floatToTime12(times[i], true));
            } else {
                result.add(floatToTime24(times[i]));
            }
        }
        return result;
    }

    private double[] adjustHighLatTimes(double[] times) {
        double nightTime = timeDiff(times[4], times[1]);

        double FajrDiff = nightPortion(methodParams.get(this.getCalcMethod())[0])
                * nightTime;

        if (Double.isNaN(times[0]) || timeDiff(times[0], times[1]) > FajrDiff) {
            times[0] = times[1] - FajrDiff;
        }

        double IshaAngle = (methodParams.get(this.getCalcMethod())[3] == 0) ? methodParams
                .get(this.getCalcMethod())[4] : 18;
        double IshaDiff = this.nightPortion(IshaAngle) * nightTime;
        if (Double.isNaN(times[6])
                || this.timeDiff(times[4], times[6]) > IshaDiff) {
            times[6] = times[4] + IshaDiff;
        }

        double MaghribAngle = (methodParams.get(this.getCalcMethod())[1] == 0) ? methodParams
                .get(this.getCalcMethod())[2] : 4;
        double MaghribDiff = nightPortion(MaghribAngle) * nightTime;
        if (Double.isNaN(times[5])
                || this.timeDiff(times[4], times[5]) > MaghribDiff) {
            times[5] = times[4] + MaghribDiff;
        }

        return times;
    }

    private double nightPortion(double angle) {
        double calc = 0;

        if (adjustHighLats == AngleBased)
            calc = (angle) / 60.0;
        else if (adjustHighLats == MidNight)
            calc = 0.5;
        else if (adjustHighLats == OneSeventh)
            calc = 0.14286;

        return calc;
    }

    private double[] dayPortion(double[] times) {
        for (int i = 0; i < 7; i++) {
            times[i] /= 24;
        }
        return times;
    }


    public void tune(int[] offsetTimes) {

        for (int i = 0; i < offsetTimes.length; i++) {




            this.offsets[i] = offsetTimes[i];
        }
    }

    private double[] tuneTimes(double[] times) {
        for (int i = 0; i < times.length; i++) {
            times[i] = times[i] + this.offsets[i] / 60.0;
        }

        return times;
    }

    public int getCalcMethod() {
        return calcMethod;
    }

    public void setCalcMethod(int calcMethod) {
        this.calcMethod = calcMethod;
    }

    public int getAsrJuristic() {
        return asrJuristic;
    }

    public void setAsrJuristic(int asrJuristic) {
        this.asrJuristic = asrJuristic;
    }

    public int getDhuhrMinutes() {
        return dhuhrMinutes;
    }

    public void setDhuhrMinutes(int dhuhrMinutes) {
        this.dhuhrMinutes = dhuhrMinutes;
    }

    public int getAdjustHighLats() {
        return adjustHighLats;
    }

    public void setAdjustHighLats(int adjustHighLats) {
        this.adjustHighLats = adjustHighLats;
    }

    public int getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(int timeFormat) {
        this.timeFormat = timeFormat;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(double timeZone) {
        this.timeZone = timeZone;
    }

    public double getJDate() {
        return JDate;
    }

    public void setJDate(double jDate) {
        JDate = jDate;
    }

    private int getJafari() {
        return Jafari;
    }

    private void setJafari(int jafari) {
        Jafari = jafari;
    }

    private int getKarachi() {
        return Karachi;
    }

    private void setKarachi(int karachi) {
        Karachi = karachi;
    }

    private int getISNA() {
        return ISNA;
    }

    private void setISNA(int iSNA) {
        ISNA = iSNA;
    }

    private int getMWL() {
        return MWL;
    }

    private void setMWL(int mWL) {
        MWL = mWL;
    }

    private int getMakkah() {
        return Makkah;
    }

    private void setMakkah(int makkah) {
        Makkah = makkah;
    }

    private int getEgypt() {
        return Egypt;
    }

    private void setEgypt(int egypt) {
        Egypt = egypt;
    }

    private int getCustom() {
        return Custom;
    }

    private void setCustom(int custom) {
        Custom = custom;
    }

    private int getTehran() {
        return Tehran;
    }

    private void setTehran(int tehran) {
        Tehran = tehran;
    }

    private int getShafii() {
        return Shafii;
    }

    private void setShafii(int shafii) {
        Shafii = shafii;
    }

    private int getHanafi() {
        return Hanafi;
    }

    private void setHanafi(int hanafi) {
        Hanafi = hanafi;
    }

    private int getNone() {
        return None;
    }

    private void setNone(int none) {
        None = none;
    }

    private int getMidNight() {
        return MidNight;
    }

    private void setMidNight(int midNight) {
        MidNight = midNight;
    }

    private int getOneSeventh() {
        return OneSeventh;
    }

    private void setOneSeventh(int oneSeventh) {
        OneSeventh = oneSeventh;
    }

    private int getAngleBased() {
        return AngleBased;
    }

    private void setAngleBased(int angleBased) {
        AngleBased = angleBased;
    }

    private int getTime24() {
        return Time24;
    }

    private void setTime24(int time24) {
        Time24 = time24;
    }

    private int getTime12() {
        return Time12;
    }

    private void setTime12(int time12) {
        Time12 = time12;
    }

    private int getTime12NS() {
        return Time12NS;
    }

    private void setTime12NS(int time12ns) {
        Time12NS = time12ns;
    }

    private int getFloating() {
        return Floating;
    }

    private void setFloating(int floating) {
        Floating = floating;
    }

    private int getNumIterations() {
        return numIterations;
    }

    private void setNumIterations(int numIterations) {
        this.numIterations = numIterations;
    }

    public ArrayList<String> getTimeNames() {
        return timeNames;
    }
}