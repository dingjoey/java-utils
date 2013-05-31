package com.taobao.joey.crypto;

import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-29
 * Time: ÏÂÎç3:07
 */
public class DigtialSignature {

    Signature signature = null;

    public DigtialSignature() {
        try {
            this.signature = Signature.getInstance("DSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            String dsaPrvKeyPem = "MIHGAgEAMIGoBgcqhkjOOAQBMIGcAkEAmELmhQxi3oyPZNWMfeGhPfa7MhhmOHbaeYRQH9gYghYxmt6ryVzv8c7AAKypsNh7Gbk5uxIOwq4nQjwcRcRLbwIVAOf5G/ZH4hLuN/zoB5b2yT19qk6zAkB97V7SP4sYaPitlQk82lr1bAbmDg77SvpWJor0VGzJ93fi3UHGfqS920b7MGIqbL8orKpuKaHr+MpOUyWBbFk/BBYCFDfwhLAwyu3WGn3kCa2bQWL9ggMa";
            String dsaPubKeyPem = "MIHwMIGoBgcqhkjOOAQBMIGcAkEAmELmhQxi3oyPZNWMfeGhPfa7MhhmOHbaeYRQH9gYghYxmt6ryVzv8c7AAKypsNh7Gbk5uxIOwq4nQjwcRcRLbwIVAOf5G/ZH4hLuN/zoB5b2yT19qk6zAkB97V7SP4sYaPitlQk82lr1bAbmDg77SvpWJor0VGzJ93fi3UHGfqS920b7MGIqbL8orKpuKaHr+MpOUyWBbFk/A0MAAkBHJf9Jvh1GcVpzQ+Qq9Ib8Uv990upzSTPclqkgqPEj3O3ugUATJOg/rJkPvauUr0XUbdvJ+GMGTYhtgMfYA/Ds";
            //KeyPair key = loadKeyPair(dsaPrvKeyPem, dsaPubKeyPem);
            KeyPair key = computeKeyPair("DSA");


            DigtialSignature ds = new DigtialSignature();
            String msg = "£¡¶¡ÇÇÒãtestÏûÏ¢£¤";
            byte[] src = msg.getBytes("UTF-8");

            /*
            byte[] sig1 = ds.computeSignature(src, key.getPrivate());
            boolean verified = ds.verifySignature(src, sig1, key.getPublic());

            System.out.println("Msg : " + msg);
            System.out.println("Sig : " + new String(sig1));
            System.out.println("verified : " + verified);

            BASE64Encoder base64Encoder = new BASE64Encoder();
            String base64PrivateKey = base64Encoder.encode(key.getPrivate().getEncoded());
            String base64PublicKey = base64Encoder.encode(key.getPublic().getEncoded());
            System.out.println("private key format : " + key.getPrivate().getFormat());
            System.out.println("private key base64 encoded:" + base64PrivateKey);
            System.out.println("public key format : " + key.getPublic().getFormat());
            System.out.println("public key base64 encoded:" + base64PublicKey);
              */


            BASE64Decoder base64Decoder = new BASE64Decoder();
            KeyFactory factory = KeyFactory.getInstance("DSA");
            PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(base64Decoder.decodeBuffer(dsaPrvKeyPem)));
            PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(base64Decoder.decodeBuffer(dsaPubKeyPem)));

            byte[] sig2 = ds.computeSignature(src, key.getPrivate());
            boolean verified = ds.verifySignature(src, sig2, key.getPublic());

            System.out.println("Msg : " + msg);
            System.out.println("Sig : " + new String(sig2));
            System.out.println("verified : " + verified);

/*


            //KeyPair key = computeKeyPair("RSA");

            DigtialSignature ds = new DigtialSignature();

            String msg = "£¡¶¡ÇÇÒãtestÏûÏ¢£¤";
            byte[] src = msg.getBytes("UTF-8");

            byte[] sig1 = ds.computeSignature(src, key.getPrivate());
            boolean verified = ds.verifySignature(src, sig1, key.getPublic());

            System.out.println("Msg : " + msg);
            System.out.println("Sig : " + new String(sig1));
            System.out.println("verified : " + verified);


            System.out.println("dump RSA private Key");
            KeyPersistence.dumpKeyInfo(key.getPrivate());
            System.out.println("dump RSA public Key");
            KeyPersistence.dumpKeyInfo(key.getPublic());


            msg = "t";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1024 * 1024; i++) {
                sb.append(msg);
            }
            src = sb.toString().getBytes("UTF-8");
            System.out.println("Msg byte size : " + src.length);// 4MB

            long startSig = System.currentTimeMillis();
            byte[] sig2 = ds.computeSignature(src, key.getPrivate());
            long endSig = System.currentTimeMillis();
            System.out.println("Sig comsumes : " + (endSig - startSig) + "ms");

            startSig = System.currentTimeMillis();
            verified = ds.verifySignature(src, sig2, key.getPublic());
            endSig = System.currentTimeMillis();
            System.out.println("Verify comsumes : " + (endSig - startSig) + "ms");

            System.out.println("Msg : " + msg);
            System.out.println("Sig : " + new String(sig2));
            System.out.println("verified : " + verified);


            msg = "abcdefghij";
            sb = new StringBuilder();
            for (int i = 0; i < 1024 * 1024 * 2; i++) {
                sb.append(msg);
            }
            src = sb.toString().getBytes("UTF-8");
            System.out.println("Msg byte size : " + src.length);// 4MB

            startSig = System.currentTimeMillis();
            byte[] sig3 = ds.computeSignature(src, key.getPrivate());
            endSig = System.currentTimeMillis();
            System.out.println("Sig comsumes : " + (endSig - startSig) + "ms");

            startSig = System.currentTimeMillis();
            verified = ds.verifySignature(src, sig3, key.getPublic());
            endSig = System.currentTimeMillis();
            System.out.println("Verify comsumes : " + (endSig - startSig) + "ms");

            System.out.println("Msg : " + msg);
            System.out.println("Sig : " + new String(sig3));
            System.out.println("verified : " + verified);
 */
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static KeyPair computeKeyPair(String algorithm) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
        keyGen.initialize(1024);
        KeyPair key = keyGen.generateKeyPair();
        return key;
    }

    public static KeyPair loadKeyPair(String prvKey, String pubKey) throws NoSuchAlgorithmException, InvalidKeySpecException {

        return null;
    }

    public byte[] computeSignature(byte[] src, PrivateKey privateKey) throws InvalidKeyException, SignatureException {
        if (signature == null) {
            throw new IllegalStateException("");
        }
        if (privateKey == null) {
            throw new IllegalStateException("");
        }

        signature.initSign(privateKey);

        signature.update(src);

        return signature.sign();
    }

    public boolean verifySignature(byte[] src, byte[] sig, PublicKey publicKey) throws InvalidKeyException, SignatureException {
        if (signature == null) {
            throw new IllegalStateException("");
        }
        if (publicKey == null) {
            throw new IllegalStateException("");
        }

        signature.initVerify(publicKey);

        signature.update(src);

        return signature.verify(sig);
    }

}
