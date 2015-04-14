package com.drawguess.util;

import java.lang.reflect.Field;

import com.drawguess.R;
import com.drawguess.R.drawable;


/**
 * 图片工具类
 * @author GuoJun
 *
 */
public class ImageUtils {

    /**
     * 获取drawble中指定文件名的ID
     * 
     * @param 文件名
     * @return 文件对应ID
     * 
     */
    public static int getImageID(String imgName) {
        Class<drawable> draw = R.drawable.class;
        try {
            Field field = draw.getDeclaredField(imgName);
            return field.getInt(imgName);
        }
        catch (SecurityException e) {
            return R.drawable.app_icon;
        }
        catch (NoSuchFieldException e) {
            return R.drawable.app_icon;
        }
        catch (IllegalArgumentException e) {
            return R.drawable.app_icon;
        }
        catch (IllegalAccessException e) {
            return R.drawable.app_icon;
        }
    }
   
}
