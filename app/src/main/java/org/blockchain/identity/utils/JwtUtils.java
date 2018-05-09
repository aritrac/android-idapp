package org.blockchain.identity.utils;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import java.security.PrivateKey;

public class JwtUtils {

    public static String createJwtToken(String serverUrl, String email, String mobilePublicKey, PrivateKey mobilePrivateKey) {
        try {
            JwtClaims claims = new JwtClaims();
            claims.setIssuer("MO");
            claims.setAudience(serverUrl);
            claims.setExpirationTimeMinutesInTheFuture(10);
            claims.setGeneratedJwtId();
            claims.setIssuedAtToNow();
            claims.setNotBeforeMinutesInThePast(2);
            claims.setSubject(email);
            claims.setStringClaim("email", email);
            claims.setStringClaim("publicKey", mobilePublicKey);

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(mobilePrivateKey);
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

            String jwt = jws.getCompactSerialization();
            return jwt;
        } catch (JoseException e) {
            e.printStackTrace();
        }
        return null;
    }
}

