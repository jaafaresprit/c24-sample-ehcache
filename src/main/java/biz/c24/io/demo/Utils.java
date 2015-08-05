package biz.c24.io.demo;


import org.plei.prelei_schema.xsd.LEIRegistration;

import java.nio.ByteBuffer;

public class Utils {

    private static int idOffset = -1;
    private static org.plei.prelei_schema.xsd.sdo.LEIRegistration sdo = null;

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

    public static org.plei.prelei_schema.xsd.sdo.LEIRegistration cloneSdoWithIdentifier(org.plei.prelei_schema.xsd.sdo.LEIRegistration sdo, int id) {
        byte[] bytes = sdo.getSdoData().clone();

        if(sdo != Utils.sdo) {
            byte[] pattern = sdo.getLegalEntityIdentifier().getBytes();
            int startOffset = -1;
            for(int i=0; i < bytes.length; i++) {
                startOffset = i;
                for(int j = 0; j < pattern.length && j+i < bytes.length; j++) {
                    if(bytes[i+j] != pattern[j]) {
                        startOffset = -1;
                        break;
                    }
                }
                if(startOffset >= 0) {
                    break;
                }
            }

            Utils.sdo = sdo;
            idOffset = startOffset;
        }

        char[] newId = String.format("%020x", id).toCharArray();

        for(int i = 0; i < newId.length; i++) {
            bytes[idOffset + i] = (byte) newId[i];
        }

        org.plei.prelei_schema.xsd.sdo.LEIRegistration sdo2 = new org.plei.prelei_schema.xsd.sdo.LEIRegistration(bytes, 1);
        return sdo2;


    }
}
