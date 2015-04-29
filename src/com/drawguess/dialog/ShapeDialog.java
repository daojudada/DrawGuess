package com.drawguess.dialog;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.drawguess.R;
import com.drawguess.interfaces.OnShapeChangedListener;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.SimpleAdapter;

public class ShapeDialog{
	
	Context context;
	private int[] images=new int[]{
			R.drawable.btn_free3,
			R.drawable.btn_line3,
			R.drawable.btn_rect3,
			R.drawable.btn_oval3};
	
	private OnShapeChangedListener mListener;
	private String[] names=new String[]{"涂鸦","直线","矩形","椭圆"};

    /**
     * 形状选择
     * @param context
     * @param title 
     * @param listener 
     */
    public ShapeDialog(Context context, OnShapeChangedListener listener) {
    	mListener=listener;

    	
    	Builder b=new AlertDialog.Builder(context);
        List<Map<String, Object>> itemlist=new ArrayList<Map<String,Object>>();
		for(int i=0;i<names.length;i++){
			Map<String, Object> item=new HashMap<String, Object>();
			item.put("image", images[i]);
			item.put("name", names[i]);
			itemlist.add(item);
		}
		SimpleAdapter simpleAdapter=new SimpleAdapter(context, itemlist, R.layout.dialog_shape, new String[]{"image","name"}, new int[]{R.id.image_item,R.id.text_item});
		b.setAdapter(simpleAdapter, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(mListener!=null)
					mListener.shapeChanged(which);
			}
		});
		
		b.create().show();
        
    }

	
}

