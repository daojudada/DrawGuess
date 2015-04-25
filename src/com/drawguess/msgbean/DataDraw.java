package com.drawguess.msgbean;



/**
 * 消息实体类，猜词用
 * @author GuoJun
 * 
 */
public class DataDraw extends Entity {
    /** 消息内容类型 **/
    public enum OP_TYPE {
        DRAW, FILL, TRANS,
        SHAPE, PACK, ERASE, EDIT, COPY, DELETE, UNDO, REDO, PAINT;
    }
    public enum TOUCH_TYPE {
        DEFAULT, DOWN1, DOWN2, MOVE, UP2, UP1
    }
    private OP_TYPE opType;
    private TOUCH_TYPE touchType;
    private int data1;
    private int data2;
    private int data3;

    public DataDraw(){
    	
    }
    
    public DataDraw(OP_TYPE opType, TOUCH_TYPE touchType, int d1, int d2, int d3) {
    	this.opType = opType;
    	this.touchType = touchType;
    	this.data1 = d1;
    	this.data2 = d2;
    	this.data3 = d3;
    }


    /**
     * 获取消息内容类型
     * 
     * @return
     * @see OP_TYPE
     */
    public OP_TYPE getOpType() {
        return this.opType;
    }

    /**
     * 获取消息内容类型
     * 
     * @return
     * @see TOUCH_TYPE
     */
    public TOUCH_TYPE getTouchType() {
        return this.touchType;
    }

    public int getData1(){
    	return this.data1;
    }


    public int getData2(){
    	return this.data2;
    }

    public int getData3(){
    	return this.data3;
    }

    /**
     * 设置消息内容类型
     * 
     * @return
     * @see OP_TYPE
     */
    public void setOpType(OP_TYPE opType) {
        this.opType = opType;
    }

    /**
     * 设置消息内容类型
     * 
     * @return
     * @see TOUCH_TYPE
     */
    public void setTouchType(TOUCH_TYPE touchType) {
        this.touchType = touchType;
    }


    public void setData1(int data1){
    	this.data1 = data1;
    }


    public void setData2(int data2){
    	this.data2 = data2;
    }

    public void setData3(int data3){
    	this.data3 = data3;
    }
    
}
