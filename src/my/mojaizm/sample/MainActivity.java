package my.mojaizm.sample;

import my.mojaizm.common.ViewTween;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class MainActivity extends Activity implements OnTouchListener {
    private View mView;
    private int mStartX;
    private int mStartY;
    private int mDragOfsX;
    private int mDragOfsY;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.View01).setOnTouchListener(this);
        findViewById(R.id.View02).setOnTouchListener(this);
        mView = (View)findViewById(R.id.View01);
    }

    public void onClickButton01(View v) {
        ViewTween.to(mView, 0, 240, 500l, new DecelerateInterpolator());
    }

    public void onClickButton02(View v) {
        ViewTween.to(mView, 320, 0, 500l, new AnticipateInterpolator());
    }

    @Override
    public boolean onTouch(View vw, MotionEvent ev) {
        int px = (int) ev.getRawX();
        int py = (int) ev.getRawY();
        
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            ((ViewGroup)vw.getParent()).bringChildToFront(vw); // z order
            
            mView = vw;
            mStartX = vw.getLeft();
            mStartY = vw.getTop();
            mDragOfsX = px;
            mDragOfsY = py;
            ViewTween.cancel(vw);
        }
        else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            int to_x = mStartX + px - mDragOfsX;
            int to_y = mStartY + py - mDragOfsY;
            
            vw.layout(to_x, to_y, to_x + vw.getWidth(), to_y + vw.getHeight());
        }
        return true;
    }
}