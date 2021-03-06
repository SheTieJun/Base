package shetj.me.base.utils;

import android.content.Context;

import androidx.annotation.Keep;

/**
 * @author shetj
 */
@Keep
public class ResourceUtils {

    public static int getIdByName(Context context, String className, String resName) {
        context = context.getApplicationContext();
        String packageName = context.getPackageName();
        return context.getResources().getIdentifier(resName, className, packageName);
    }
}
