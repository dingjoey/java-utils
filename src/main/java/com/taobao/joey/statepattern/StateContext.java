package com.taobao.joey.statepattern;

/**
 * Created with IntelliJ IDEA.
 * User: qiaoyi.dingqy
 * Date: 13-5-9
 * Time: ����10:30
 * To change this template use File | Settings | File Templates.
 */
public class StateContext {

    public IState currentState = null;
    public int bizProperty = -1;

    public StateContext(IState state) {
        if (state != null) {
            currentState = state;
        } else {
            currentState = ConcreteStateA.getInstance();
        }
    }

    /**
     * ϵͳ�ⲿmessage
     *
     * @param message
     */
    public void request(Object message) {
        // convert message to certain context property   && ״̬Ǩ����State������
        bizProperty = ((String) message).length();

        // fire currentState action
        currentState.handle(this);
    }


}
