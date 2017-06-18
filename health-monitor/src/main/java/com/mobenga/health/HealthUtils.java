package com.mobenga.health;


import javax.xml.bind.JAXB;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.mobenga.health.model.ModulePK;

/**
 * Class-utility for health control and configuration services
 */
public final class HealthUtils {
    private HealthUtils() {}
    private static final SimpleDateFormat DATE_TIME_FORMATER = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
    public static String key(ModulePK application) {
        return new StringBuilder()
                .append(application.getSystemId())      .append("|")
                .append(application.getApplicationId()) .append("|")
                .append(application.getVersionId())     .toString();
    }

    public static String toXML(Object data) {
        final StringWriter xml = new StringWriter(10000);
        JAXB.marshal(data, xml);
        xml.flush();
        return xml.toString();
    }

    public static Date fromString(String date){
        try {
            return DATE_TIME_FORMATER.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
    public static String fromDate(Date date){
        return DATE_TIME_FORMATER.format(date);
    }
}
