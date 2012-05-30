package my.mojaizm.common;

import java.util.Map;
import java.util.WeakHashMap;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

public class ViewTween {
    private static String TAG = ViewTween.class.getSimpleName();
    private static final long LOOP_INTERVAL = 33;
    
    private static Map<View, Info> sInfoMap = new WeakHashMap<View, Info>();
    private static InternalHandler sMainHandler = new InternalHandler(Looper.getMainLooper());
    
    private static interface Callback {
        public void onComplete();
        public void onCancel();
    }
    
    private static class Info {
        Rect org;
        Rect delta;
        
        long dulation;
        long start_time;
        
        android.view.animation.Interpolator interpolator;
        Callback callback;
        
        public Info() {
            org = new Rect();
            delta = new Rect();
            callback = null;
        }
    }
    
    private static class InternalHandler extends Handler {
        @SuppressWarnings({"unchecked", "RawUseOfParameterizedType"})
        
        public InternalHandler(Looper lp) {
            super(lp);
        }
        
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage !!!!!!!!!!!!!!!!");
            
            for (View vw : sInfoMap.keySet()) {
                
                Info info = (Info)sInfoMap.get(vw);
                
                if (vw.getParent() == null || info == null) {
                    sInfoMap.remove(vw);
                    continue;
                }
                
                float progress = (float)(System.currentTimeMillis() - info.start_time) / (float)info.dulation;
                if (progress > 1.0f) {
                    progress = 1.0f;
                }
                float calc = info.interpolator.getInterpolation(progress);
                int l = (int)(info.org.left + info.delta.left * calc);
                int t = (int)(info.org.top + info.delta.top * calc);
                int r = (int)(l + info.org.width());// + info.delta.right * calc);
                int b = (int)(t + info.org.height());// + info.delta.bottom * calc);
                vw.layout(l, t, r, b);
                
                if (progress < 1.0f) {
                    continue;
                }
                sInfoMap.remove(vw);
                if (info.callback != null) {
                    info.callback.onComplete();
                }
            }
            
            if (sInfoMap.size() <= 0) {
                return;
            }
            sMainHandler.removeMessages(0);
            sMainHandler.sendEmptyMessageDelayed(0, LOOP_INTERVAL);
        }
    }
    
    public static void cancel(View vw) {
        Info info = sInfoMap.get(vw);
        if (info == null) {
            return;
        }
        sInfoMap.remove(vw);
        if (info.callback != null) {
            info.callback.onCancel();
        }
        info = null;
    }
    
    public static void to(View vw, int to_x, int to_y, long dulation, android.view.animation.Interpolator interpolator) {
        to(vw, to_x, to_y, dulation, null, interpolator);
    }
        
    public static void to(View vw, int to_x, int to_y, long dulation, Callback callback,
            android.view.animation.Interpolator interpolator) {
        
        Info info = new Info();
        
        info.org.left = vw.getLeft();
        info.org.top = vw.getTop();
        info.org.right = info.org.left + vw.getWidth();
        info.org.bottom = info.org.top + vw.getHeight();
        
        info.delta.left = to_x - info.org.left;
        info.delta.top = to_y - info.org.top;
        info.delta.right = (to_x + info.org.width()) - info.org.right;
        info.delta.bottom = (to_y + info.org.height()) - info.org.bottom;
        
        if (dulation <= 0) {
            dulation = 1;
        }
        info.dulation = dulation;
        info.start_time = System.currentTimeMillis();
        
        info.callback = callback;
        
        info.interpolator = interpolator;
            
        sInfoMap.put(vw, info);
        
        if (sMainHandler.hasMessages(0)) {
            return;
        }
        sMainHandler.sendEmptyMessage(0);
    }
}
