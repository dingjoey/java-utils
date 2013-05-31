package com.taobao.joey.statepattern;

/**
 * Created with IntelliJ IDEA.
 * User: qiaoyi.dingqy
 * Date: 13-5-9
 * Time: 上午10:30
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
     * 系统外部message
     *
     * @param message
     */
    public void request(Object message) {
        // convert message to certain context property   && 状态迁移由State负责了
        bizProperty = ((String) message).length();

        // fire currentState action
        currentState.handle(this);
    }


}
