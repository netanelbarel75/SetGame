
import java.io.*;
import java.security.*;
import java.util.*;
import java.security.cert.*;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.*;

public class GenerateKeystore {
    public static void main(String[] args) {
        try {
            // Create a new keystore
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(null, null);
            
            // Generate a key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            
            // Generate a self-signed certificate
            X509CertInfo info = new X509CertInfo();
            Date from = new Date();
            Date to = new Date(from.getTime() + 365 * 86400000L * 10); // 10 years
            CertificateValidity interval = new CertificateValidity(from, to);
            
            X500Name owner = new X500Name("CN=Android Debug,O=Android,C=US");
            info.set(X509CertInfo.VALIDITY, interval);
            info.set(X509CertInfo.SUBJECT, owner);
            info.set(X509CertInfo.ISSUER, owner);
            info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
            info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            
            AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
            info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
            
            X509CertImpl cert = new X509CertImpl(info);
            cert.sign(pair.getPrivate(), "SHA1withRSA");
            
            // Store the certificate and key in the keystore
            keystore.setKeyEntry("androiddebugkey", pair.getPrivate(), "android".toCharArray(), 
                               new Certificate[] { cert });
            
            // Write the keystore to a file
            FileOutputStream fos = new FileOutputStream("debug.keystore");
            keystore.store(fos, "android".toCharArray());
            fos.close();
            
            System.out.println("Keystore created successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
