package fr.treeptik.cloudunit.functions;

import fr.treeptik.cloudunit.utils.AlphaNumericsCharactersCheckUtils;

import java.io.UnsupportedEncodingException;

import static org.bouncycastle.crypto.tls.ConnectionEnd.server;

/**
 * Created by nicolas on 24/05/2016.
 */
public class ContainerNaming {

    public static String generateName(String cuInstanceName, String userLogin, String applicatioName, String serverName)
    throws UnsupportedEncodingException {
            return AlphaNumericsCharactersCheckUtils.convertToAlphaNumerics(cuInstanceName)
                + "-" + AlphaNumericsCharactersCheckUtils.convertToAlphaNumerics(userLogin)
                + "-" + AlphaNumericsCharactersCheckUtils.convertToAlphaNumerics(applicatioName)
                + "-" + serverName;
    }
}
