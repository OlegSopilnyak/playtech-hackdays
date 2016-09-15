package com.mobenga.health;

import com.mobenga.health.model.HealthItemPK;

import javax.xml.bind.JAXB;
import java.io.StringWriter;

/**
 * Class-utility for health control and configuration services
 */
public final class HealthUtils {
    private HealthUtils() {}
    public static String key(HealthItemPK application) {
        return new StringBuilder(application.getSystemId())
                .append("|")
                .append(application.getApplicationId())
                .append("|")
                .append(application.getVersionId())
                .toString();
    }

    public static String toXML(Object data) {
        final StringWriter xml = new StringWriter(10000);
        JAXB.marshal(data, xml);
        xml.flush();
        return xml.toString();
    }


}
