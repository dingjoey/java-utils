package com.taobao.joey.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-29
 * Time: ÏÂÎç1:48
 */
public class RSAEncryptionManager {
    private static Logger LOG = LoggerFactory.getLogger(RSAEncryptionManager.class);
    // generate pairs of public and private keys
    private KeyPairGenerator keyGen;
    //
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    //
    private Cipher cipher;

    public static void main(String[] args) {
        String msg = "$¶¡ÇÇÒãtestÏûÏ¢001!";
        String encode = "UTF-8";

        RSAEncryptionManager sem = new RSAEncryptionManager();
        RSAEncryptionManager sem1 = new RSAEncryptionManager();
        try {
            sem.init();
            sem1.init();
        } catch (Exception e) {
            LOG.warn("RSAEncryptionManager init error", e);
        }

        try {
            byte[] src = msg.getBytes(encode);
            byte[] enc = sem.encrypt(src);

            LOG.debug("src msg:" + msg);
            LOG.debug("encryption:" + new String(enc));

            byte[] dest = sem1.decrypt(enc);
            LOG.debug("desc:" + new String(dest, encode));
        } catch (UnsupportedEncodingException e) {
            LOG.warn("msg to bytes error", e);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws java.security.NoSuchAlgorithmException
     * @throws javax.crypto.NoSuchPaddingException
     */
    public void init() throws NoSuchAlgorithmException, NoSuchPaddingException {
        initKey();
        initCipher();
    }

    /**
     * @throws java.security.NoSuchAlgorithmException
     */
    private void initKey() throws NoSuchAlgorithmException {
        keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);       // RSA key at least 512 bits

        KeyPair keyPair = keyGen.generateKeyPair();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        publicKey = (RSAPublicKey) keyPair.getPublic();

        LOG.debug("private key:" + privateKey.toString());
        LOG.debug("public key:" + publicKey.toString());
    }

    /**
     * @throws javax.crypto.NoSuchPaddingException
     * @throws java.security.NoSuchAlgorithmException
     */
    private void initCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        cipher = Cipher.getInstance("RSA");
    }

    /**
     * @param src
     * @return
     * @throws java.security.InvalidKeyException
     * @throws javax.crypto.BadPaddingException
     * @throws javax.crypto.IllegalBlockSizeException
     */
    private byte[] encrypt(byte[] src) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if (cipher == null) {
            throw new IllegalStateException("cipher cannot be null£¡");
        }

        if (publicKey == null) {
            throw new IllegalStateException("publicKey cannot be null£¡");
        }

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(src);
    }

    /**
     * @param encrypt
     * @return
     * @throws java.security.InvalidKeyException
     * @throws javax.crypto.BadPaddingException
     * @throws javax.crypto.IllegalBlockSizeException
     */
    private byte[] decrypt(byte[] encrypt) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        if (cipher == null) {
            throw new IllegalStateException("cipher cannot be null£¡");
        }

        if (privateKey == null) {
            throw new IllegalStateException("privateKey cannot be null£¡");
        }

        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(encrypt);
    }
}
