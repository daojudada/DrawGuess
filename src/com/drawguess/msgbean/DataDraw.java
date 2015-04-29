package com.drawguess.msgbean;

import com.alibaba.fastjson.annotation.JSONField;



/**
 * 消息实体类，猜词用
 * @author GuoJun
 * 
 */
public class DataDraw extends Entity {
    /** 消息内容类型 **/
    public enum OP_TYPE {
        DRAW, FILL, TRANS,
        ERASE, PAINT, SHAPE, PACK, UNDO, REDO, CLEAR, EDIT, COPY, DELETE;
    }
    public enum TOUCH_TYPE {
        DEFAULT, DOWN1, DOWN2, MOVE, UP2, UP1
    }
    private OP_TYPE opType;
    private TOUCH_TYPE touchType;
    private float data1;
    private float data2;
    private float data3;
    private float data4;

    public DataDraw(){
    	
    }
    
    public DataDraw(OP_TYPE opType, TOUCH_TYPE touchType, float d1, float d2, float d3, float d4) {
    	this.opType = opType;
    	this.touchType = touchType;
    	this.data1 = d1;
    	this.data2 = d2;
    	this.data3 = d3;
    	this.data4 = d4;
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

    public float getData1(){
    	return this.data1;
    }

    public float getData2(){
    	return this.data2;
    }

    public float getData3(){
    	return this.data3;
    }

    public float getData4(){
    	return this.data4;
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


    public void setData1(float data1){
    	this.data1 = data1;
    }


    public void setData2(float data2){
    	this.data2 = data2;
    }

    public void setData3(float data3){
    	this.data3 = data3;
    }
    
    public void setData4(float data4){
    	this.data4 = data4;
    }
}
