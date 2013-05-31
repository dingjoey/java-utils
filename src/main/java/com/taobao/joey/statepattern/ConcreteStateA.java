package com.taobao.joey.statepattern;

/**
 * Created with IntelliJ IDEA.
 * User: qiaoyi.dingqy
 * Date: 13-5-9
 * Time: ����10:52
 * To change this template use File | Settings | File Templates.
 */
public class ConcreteStateA implements IState {

    private static final IState instance = new ConcreteStateA();

    public static IState getInstance() {
        return instance;
    }

    @Override
    public void handle(StateContext stateContext) {
        // A:do state action
        System.out.println("ConcreteStateA handling!");

        // B:transition logic judge and do state transition
        // ����Context���ԣ���ConcreteStateA�ڲ���������жϣ�����״̬Ǩ��
        // if :
        //      stateContext.currentState = nextState1
        // else if :
        //      stateContext currentState = nextState2
        // else:
        //      ��������������

        if (stateContext.bizProperty % 2 == 0) {
            stateContext.currentState = ConcreteStateB.getInstance();
        } else {
            // remain as ConcreteStateA
        }
    }

    public String toString(){
        return "ConcreteStateA";
    }
}
