package org.blockchain.identity.utils;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Iterator;
import java.util.Map;

public class JwtUtils {

    public static String createJwtToken(String sub, Map<String, String> data, PrivateKey serverPrivateKey) {
        try {
            JwtClaims claims = new JwtClaims();
            claims.setIssuer("MO");
            claims.setExpirationTimeMinutesInTheFuture(10);
            claims.setGeneratedJwtId();
            claims.setIssuedAtToNow();
            claims.setNotBeforeMinutesInThePast(2);
            claims.setSubject(sub);

            Iterator<String> keys = data.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                claims.setStringClaim(key, data.get(key));
            }

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(serverPrivateKey);

            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

            String jwt = jws.getCompactSerialization();
            return jwt;
        } catch (JoseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JwtClaims verifyJwtResponse(String jwt, PublicKey serverPublicKey) {
        try {
            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime() // the JWT must have an expiration time
                    .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
                    .setRequireSubject() // the JWT must have a subject claim
                    .setSkipDefaultAudienceValidation()
                    .setExpectedIssuer("CA") // whom the JWT needs to have been issued by
                    .setVerificationKey(serverPublicKey) // verify the signature with the public key
                    .setJwsAlgorithmConstraints( // only allow the expected signature algorithm(s) in the given context
                            new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, // which is only RS256 here
                                    AlgorithmIdentifiers.RSA_USING_SHA256))
                    .build(); // create the JwtConsumer instance
            JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
            return jwtClaims;
        } catch (InvalidJwtException e) {
            e.printStackTrace();
        }
        return null;
    }
}

