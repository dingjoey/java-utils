package com.taobao.joey.statepattern;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: qiaoyi.dingqy
 * Date: 13-5-9
 * Time: …œŒÁ10:56
 * To change this template use File | Settings | File Templates.
 */
public class Client {

    private StateContext stateContext;

    public Client() {
        // …Ë÷√≥ı º◊¥Ã¨
        stateContext = new StateContext(ConcreteStateA.getInstance());
    }

    public static void main(String[] args) {
        long timeout = TimeUnit.MINUTES.toMillis(1L);
        System.out.println(timeout);
        Client client = new Client();

        client.fireEvent("1");  //statA
        client.fireEvent("12"); //stateA
        client.fireEvent("1");  //stateB
        client.fireEvent("12"); //stateA

    }

    public void fireEvent(String msg) {
        stateContext.request(msg);
    }

}
