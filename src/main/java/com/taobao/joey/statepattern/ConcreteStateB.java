package com.taobao.joey.statepattern;

/**
 * Created with IntelliJ IDEA.
 * User: qiaoyi.dingqy
 * Date: 13-5-9
 * Time: 上午11:05
 * To change this template use File | Settings | File Templates.
 */
public class ConcreteStateB implements IState {
    private static final IState instance = new ConcreteStateB();

    public static IState getInstance() {
        return instance;
    }

    @Override
    public void handle(StateContext stateContext) {
        // A:do state action
        System.out.println("ConcreteStateB handling!");

        // B:transition logic judge and do state transition
        // 根据Context属性，和ConcreteStateA内部属性组合判断，驱动状态迁移
        // if :
        //      stateContext.currentState = nextState1
        // else if :
        //      stateContext currentState = nextState2
        // else:
        //      。。。。。。。

        if (stateContext.bizProperty % 2 == 1) {
            stateContext.currentState = ConcreteStateA.getInstance();
        } else {
            // remain as ConcreteStateB
        }
    }

    public String toString(){
        return "ConcreteStateB";
    }
}
