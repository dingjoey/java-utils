package com.taobao.joey;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: qiaoyi.dingqy
 * Date: 13-5-7
 * Time: ÏÂÎç7:56
 * To change this template use File | Settings | File Templates.
 */
public class RandomUtil {

    private static void randomWithoutSeed() {
        Random random = new Random();
        System.out.println(random.nextInt());
        System.out.println(random.nextInt());
    }

    private static void randomWithFixedSeed() {
        Random random = new Random(1);
        System.out.println(random.nextInt());
        System.out.println(random.nextInt());
    }

    private static void randomWithTimeSeed() {
        Random random = new Random(System.currentTimeMillis());
        System.out.println(random.nextInt());
        System.out.println(random.nextInt());
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<String, String>();
        Map map2 = map;
        map.put("key", "value");
        System.out.println(map2.get("key"));

        /*
        while (true) {
            randomWithoutSeed();
            System.out.println("");
            randomWithFixedSeed();
            System.out.println("");
            randomWithTimeSeed();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        */
    }
}

