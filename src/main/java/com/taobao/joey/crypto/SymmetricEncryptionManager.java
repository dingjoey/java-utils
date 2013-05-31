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
 * Time: ����10:56
 */
public class SymmetricEncryptionManager {
    private static Logger LOG = LoggerFactory.getLogger(SymmetricEncryptionManager.class);
    // ���ڴ����ԳƼ����㷨����Կ secret (symmetric) key generator
    private KeyGenerator keyGen;
    // ������ʾ�ԳƼ����㷨��Ҫ�õ�����Կ A secret (symmetric) key
    private SecretKey key;
    // ��������㷨���� ,�ṩͳһ�ļ��ܡ����ܽӿ�
    private Cipher cipher;
    // �Գ��㷨����
    private String algorithm;

    public SymmetricEncryptionManager(String algorithm) {
        this.algorithm = algorithm;
    }

    public static void main(String[] args) {
        String msg = "$������test��Ϣ001!";
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

        // Step 1:������Կ������
        // ������Կ��������������Կ��С �����Դ �ĸ���
        /**
         * ������Կ�ķ�ʽ�����֣����㷨�޹صķ�ʽ���ض����㷨�ķ�ʽ,��Ӧ����key generator�ķ���Ҳ��һ��
         *  a�����㷨�޹صĳ�ʼ�� ��new + init
         *  b�� �ض����㷨�ĳ�ʼ����getInstance
         */
        // ʹ��KeyGenerator�ṩ�Ĺ�������������Ӧ�ԳƼ����㷨��key generator
        keyGen = KeyGenerator.getInstance(algorithm);

        // Step 2:����key
        key = keyGen.generateKey();
    }

    /**
     * @throws javax.crypto.NoSuchPaddingException
     * @throws java.security.NoSuchAlgorithmException
     */
    private void initCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        /**
         * Ϊ���� Cipher ����Ӧ�ó������ Cipher �� getInstance ��������������ת�� �����ƴ��ݸ�����������ָ���ṩ�ߵ����ƣ���ѡ����
         * ת������������ʽ��
         *  1)���㷨/ģʽ/��䡱��
         *  2) ���㷨��
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
            throw new IllegalStateException("cipher cannot be null��");
        }

        if (key == null) {
            throw new IllegalStateException("secret key cannot be null��");
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
            throw new IllegalStateException("cipher cannot be null��");
        }

        if (key == null) {
            throw new IllegalStateException("secret key cannot be null��");
        }

        cipher.init(Cipher.DECRYPT_MODE, key);

        return cipher.doFinal(encrypt);
    }
}
