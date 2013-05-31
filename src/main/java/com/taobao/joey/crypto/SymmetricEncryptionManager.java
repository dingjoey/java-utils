package com.taobao.joey.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: joeyutil
 * User: qiaoyi.dingqy
 * Date: 13-5-29
 * Time: 上午10:56
 */
public class SymmetricEncryptionManager {
    private static Logger LOG = LoggerFactory.getLogger(SymmetricEncryptionManager.class);
    // 用于创建对称加密算法的密钥 secret (symmetric) key generator
    private KeyGenerator keyGen;
    // 用来表示对称加密算法中要用到的密钥 A secret (symmetric) key
    private SecretKey key;
    // 抽象加密算法的类 ,提供统一的加密、解密接口
    private Cipher cipher;
    // 对称算法名字
    private String algorithm;

    public SymmetricEncryptionManager(String algorithm) {
        this.algorithm = algorithm;
    }

    public static void main(String[] args) {
        String msg = "$丁乔毅test消息001!";
        String encode = "UTF-8";
        //String algorithm = "DES";     //DES
        //String algorithm = "AES";       // AES
        String algorithm = "DESede";  //3DES

        SymmetricEncryptionManager sem = new SymmetricEncryptionManager("IDEA");
        try {
            sem.init();
        } catch (Exception e) {
            LOG.warn("SymmetricEncryptionManager init error", e);
        }

        try {
            byte[] src = msg.getBytes(encode);
            byte[] enc = sem.encrypt(src);
            LOG.debug("src msg:" + msg);
            LOG.debug("encryption:" + new String(enc));
            byte[] dest = sem.decrypt(enc);
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

        // Step 1:构造密钥生成器
        // 所有密钥生成器都具有密钥大小 和随机源 的概念
        /**
         * 生成密钥的方式有两种：与算法无关的方式和特定于算法的方式,对应构造key generator的方法也不一样
         *  a）与算法无关的初始化 ：new + init
         *  b） 特定于算法的初始化：getInstance
         */
        // 使用KeyGenerator提供的工厂方法创建对应对称加密算法的key generator
        keyGen = KeyGenerator.getInstance(algorithm);

        // Step 2:生成key
        key = keyGen.generateKey();
    }

    /**
     * @throws javax.crypto.NoSuchPaddingException
     * @throws java.security.NoSuchAlgorithmException
     */
    private void initCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        /**
         * 为创建 Cipher 对象，应用程序调用 Cipher 的 getInstance 方法并将所请求转换 的名称传递给它。还可以指定提供者的名称（可选）。
         * 转换具有以下形式：
         *  1)“算法/模式/填充”或
         *  2) “算法”
         */
        cipher = Cipher.getInstance(algorithm);
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
            throw new IllegalStateException("cipher cannot be null！");
        }

        if (key == null) {
            throw new IllegalStateException("secret key cannot be null！");
        }

        cipher.init(Cipher.ENCRYPT_MODE, key);

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
            throw new IllegalStateException("cipher cannot be null！");
        }

        if (key == null) {
            throw new IllegalStateException("secret key cannot be null！");
        }

        cipher.init(Cipher.DECRYPT_MODE, key);

        return cipher.doFinal(encrypt);
    }
}
