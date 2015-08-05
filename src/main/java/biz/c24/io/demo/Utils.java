package biz.c24.io.demo;

import org.plei.prelei_schema.xsd.LEIRegistration;

public class Utils {


    public static LEIRegistration cloneWithIdentifier(LEIRegistration cdo, int id) {
        LEIRegistration ret;
        try {
            ret = (LEIRegistration) cdo.cloneDeep();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        String newId = String.format("%020x", id);

        ret.setLegalEntityIdentifier(newId);

        return ret;
    }


}
