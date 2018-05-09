package org.blockchain.identity.feature;

import org.blockchain.identity.utils.KeyUtils;

public class MobileKeys {

    private static final KeyUtils.StringKeyPair keyPair;

    static {
        keyPair = KeyUtils.generateRSASHA256Certs();
    }

    public static String getMobilePublicKey() {
        return keyPair.getPublicKey();
    }

    public static String getMobilePrivateKey() {
        return keyPair.getPrivateKey();
    }
}
