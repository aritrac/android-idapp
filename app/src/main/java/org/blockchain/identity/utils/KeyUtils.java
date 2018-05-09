package org.blockchain.identity.utils;

import android.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

public class KeyUtils {

    private static final int KEY_SIZE = 2048;
    private static final String CERT_TYPE = "RSA";
    private static final String SECURE_RANDOM_INSTANCE_TYPE = "SHA1PRNG";
    private static final String ORGANIZATION_NAME = "CA";
    private static final String COUNTRY_CODE = "IN";

    public static StringKeyPair generateRSASHA256Certs() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(CERT_TYPE);
            //Requesting a Secure Random instance with SHA256 algo
            SecureRandom random = SecureRandom.getInstance(SECURE_RANDOM_INSTANCE_TYPE);
            //Initialise a key pair generator
            keyGen.initialize(KEY_SIZE, random);
            //Generating private and public keys and storing them in PrivateKey and PublicKey Objects.
            KeyPair pair = keyGen.generateKeyPair();
            return new StringKeyPair(getPrivateKeyString(pair.getPrivate()), getX509CertificateString(pair));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatCertificate(String certificate) {
        String BEGIN_CERTIFICATE = "BEGIN CERTIFICATE";
        String END_CERTIFICATE = "END CERTIFICATE";
        if (StringUtils.isNotBlank(certificate)) {
            certificate = StringUtils.remove(certificate, "\r");
            certificate = StringUtils.remove(certificate, "\n");
            certificate = StringUtils.remove(certificate, "-");
            certificate = StringUtils.remove(certificate, BEGIN_CERTIFICATE);
            certificate = StringUtils.remove(certificate, END_CERTIFICATE);
            certificate = StringUtils.remove(certificate, " ");
            certificate = Base64.encodeToString(Base64.decode(certificate, Base64.DEFAULT), Base64.DEFAULT);
            StringBuilder cert = new StringBuilder();
            cert.append("-----").append(BEGIN_CERTIFICATE).append("-----\r\n");
            cert.append(certificate);
            cert.append("-----").append(END_CERTIFICATE).append("-----");
            return cert.toString();
        }
        return certificate;
    }

    public static String formatPrivateKey(String certificate) {
        String BEGIN_CERTIFICATE = "BEGIN PRIVATE KEY";
        String BEGIN_RSA_CERTIFICATE = "BEGIN RSA PRIVATE KEY";
        String END_CERTIFICATE = "END PRIVATE KEY";
        String END_RSA_CERTIFICATE = "END RSA PRIVATE KEY";
        if (StringUtils.isNotBlank(certificate)) {
            certificate = StringUtils.remove(certificate, "\r");
            certificate = StringUtils.remove(certificate, "\n");
            certificate = StringUtils.remove(certificate, "-");
            certificate = StringUtils.remove(certificate, BEGIN_CERTIFICATE);
            certificate = StringUtils.remove(certificate, BEGIN_RSA_CERTIFICATE);
            certificate = StringUtils.remove(certificate, END_CERTIFICATE);
            certificate = StringUtils.remove(certificate, END_RSA_CERTIFICATE);
            certificate = StringUtils.remove(certificate, " ");
            certificate = Base64.encodeToString(Base64.decode(certificate, Base64.DEFAULT), Base64.DEFAULT);
            StringBuffer cert = new StringBuffer("-----" + BEGIN_CERTIFICATE + "-----\r\n");
            cert.append(certificate);
            cert.append("-----" + END_CERTIFICATE + "-----");
            return cert.toString();
        }
        return certificate;
    }

    public static PrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        if (StringUtils.isNotBlank(privateKey)) {
            privateKey = deserializePrivateCertificate(privateKey);
            byte[] bytes = Base64.decode(privateKey, Base64.DEFAULT);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        }
        return null;
    }

    public static PublicKey getPublicKey(String publicKey) throws NoSuchAlgorithmException,
            InvalidKeySpecException, IOException, CertificateException {
        if (StringUtils.isNotBlank(publicKey)) {
            publicKey = formatCertificate(publicKey);
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            InputStream is = new ByteArrayInputStream(publicKey.getBytes());
            X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
            PublicKey key = cer.getPublicKey();
            return key;
        }
        return null;
    }

    public static String deserializePrivateCertificate(String certificate) {
        String BEGIN_CERTIFICATE = "BEGIN PRIVATE KEY";
        String BEGIN_RSA_CERTIFICATE = "BEGIN RSA PRIVATE KEY";
        String END_CERTIFICATE = "END PRIVATE KEY";
        String END_RSA_CERTIFICATE = "END RSA PRIVATE KEY";
        if (StringUtils.isNotBlank(certificate)) {
            certificate = StringUtils.remove(certificate, "\r");
            certificate = StringUtils.remove(certificate, "\n");
            certificate = StringUtils.remove(certificate, "-");
            certificate = StringUtils.remove(certificate, BEGIN_CERTIFICATE);
            certificate = StringUtils.remove(certificate, BEGIN_RSA_CERTIFICATE);
            certificate = StringUtils.remove(certificate, END_CERTIFICATE);
            certificate = StringUtils.remove(certificate, END_RSA_CERTIFICATE);
            certificate = StringUtils.remove(certificate, " ");
        }
        return certificate;
    }

    public static String deserializePublicCertificate(String certificate) {
        String BEGIN_CERTIFICATE = "BEGIN CERTIFICATE";
        String END_CERTIFICATE = "END CERTIFICATE";
        if (StringUtils.isNotBlank(certificate)) {
            certificate = StringUtils.remove(certificate, "\r");
            certificate = StringUtils.remove(certificate, "\n");
            certificate = StringUtils.remove(certificate, "-");
            certificate = StringUtils.remove(certificate, BEGIN_CERTIFICATE);
            certificate = StringUtils.remove(certificate, END_CERTIFICATE);
            certificate = StringUtils.remove(certificate, " ");
        }
        return certificate;
    }

    public static String getPrivateKeyString(PrivateKey privateKey) {
        return android.util.Base64.encodeToString(privateKey.getEncoded(), android.util.Base64.DEFAULT);
    }

    public static String getX509CertificateString(KeyPair keyPair) {
        try {
            X509Certificate x509Certificate = generateCertificate(keyPair);
            return android.util.Base64.encodeToString(x509Certificate.getEncoded(), android.util.Base64.DEFAULT);
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static X509Certificate generateCertificate(KeyPair pair) {
        try {
            Date from = new Date(System.currentTimeMillis());
            Date to = new Date(System.currentTimeMillis() + 86400000L * 365 * 10);
            BigInteger sn = new BigInteger(64, new SecureRandom());
            X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
            X500Principal dnName = new X500Principal("CN=CA, OU=CA, O=CA, C=IN");
            certGen.setSerialNumber(sn);
            certGen.setIssuerDN(dnName);
            certGen.setNotBefore(from);
            certGen.setNotAfter(to);
            certGen.setSubjectDN(dnName);                       // note: same as issuer
            certGen.setPublicKey(pair.getPublic());
            certGen.setSignatureAlgorithm("SHA256withRSA");
            X509Certificate cert = certGen.generate(pair.getPrivate(), new SecureRandom());
            return cert;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Boolean isValidCertificate(String certificate) {
        certificate = formatCertificate(certificate);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cf.generateCertificate(new ByteArrayInputStream(certificate.getBytes()));
            return Boolean.TRUE;
        } catch (CertificateException e) {
            return Boolean.FALSE;
        }
    }

    public static class StringKeyPair {
        private String privateKey;
        private String publicKey;

        public StringKeyPair(final String privateKey, final String publicKey) {
            this.privateKey = privateKey;
            this.publicKey = formatCertificate(publicKey);
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }
    }
}

