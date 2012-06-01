package my.mojaizm.common;

import java.util.Map;
import java.util.WeakHashMap;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

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
        
        long duration;
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
        public InternalHandler(Looper lp) {
            super(lp);
        }
        
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage !!!!!!!!!!!!!!!!");
            
            for (View vw : sInfoMap.keySet()) {
                
                Info info = sInfoMap.get(vw);
                
                if (vw.getParent() == null || info == null) {
                    sInfoMap.remove(vw);
                    continue;
                }
                
                float progress = (float)(System.currentTimeMillis() - info.start_time) / (float)info.duration;
                if (progress > 1.0f) {
                    progress = 1.0f;
                }
                float calc = info.interpolator.getInterpolation(progress);
                int l = (int)(info.org.left + info.delta.left * calc);
                int t = (int)(info.org.top + info.delta.top * calc);
                int r = (int)(info.org.right + info.delta.right * calc);
                int b = (int)(info.org.bottom + info.delta.bottom * calc);
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
        
        Animation anim = vw.getAnimation();
        if (anim != null) {
            anim.cancel();
        }
        if (info.callback != null) {
            info.callback.onCancel();
        }
        info = null;
    }
    
    public static void to(View view, long duration, int to_x, int to_y) {
        to(view, duration,
            to_x, to_y, view.getWidth(), view.getHeight(),
            null,
            new LinearInterpolator());
    }
        
    public static void to(View view, long duration,
            int to_x, int to_y, int to_w, int to_h,
            Callback callback,
            android.view.animation.Interpolator interpolator) {
        
        Info info = new Info();
        
        info.org.left = view.getLeft();
        info.org.top = view.getTop();
        info.org.right = info.org.left + view.getWidth();
        info.org.bottom = info.org.top + view.getHeight();
        
        info.delta.left = to_x - info.org.left;
        info.delta.top = to_y - info.org.top;
        info.delta.right = to_x + to_w - info.org.right;
        info.delta.bottom = to_y + to_h - info.org.bottom;
        
        if (duration <= 0) {
            duration = 1;
        }
        info.duration = duration;
        info.start_time = System.currentTimeMillis();
        
        info.callback = callback;
        
        info.interpolator = interpolator;
            
        sInfoMap.put(view, info);
        
        if (view.getAnimation() != null) {
            view.getAnimation().cancel();
        }
        
        if (sMainHandler.hasMessages(0)) {
            return;
        }
        sMainHandler.sendEmptyMessage(0);
    }
    
    public static void anim(View view, long duration,
            float from_alpha, float to_alpha,
            float from_ang, float to_ang,
            final Callback callback,
            android.view.animation.Interpolator interpolator) {
            
        AnimationSet an = null;
        if (from_alpha != to_alpha) {
            if (an == null) {
                an = new AnimationSet(true);
            }
            an.addAnimation(new AlphaAnimation(from_alpha, to_alpha));
        }
        if (from_ang != to_ang) {
            if (an == null) {
                an = new AnimationSet(true);
            }
            an.addAnimation(new RotateAnimation(from_ang, to_ang,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f));
        }
        if (an != null) {
            an.setDuration(duration);
            an.setInterpolator(interpolator);
            an.setFillBefore(true);
            an.setFillAfter(true);
            if (callback != null) {
                an.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        callback.onComplete();
                    }
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
            }
            view.startAnimation(an);
        }
    }
}
