package my.mojaizm.sample;

import my.mojaizm.common.ViewTween;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
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

        mView = (View) findViewById(R.id.View01);
        mView.setOnTouchListener(this);
    }

    public void onClickButton01(View v) {
        ViewTween.to(mView, 0, 240, 600l, new DecelerateInterpolator());
    }

    public void onClickButton02(View v) {
        ViewTween.to(mView, 320, 0, 600l, new AnticipateInterpolator());
    }

    @Override
    public boolean onTouch(View vw, MotionEvent ev) {
        int px = (int) ev.getRawX();
        int py = (int) ev.getRawY();
        
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mStartX = vw.getLeft();
            mStartY = vw.getTop();
            mDragOfsX = px;
            mDragOfsY = py;
        }
        else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            int to_x = mStartX + px - mDragOfsX;
            int to_y = mStartY + py - mDragOfsY;
            
            vw.layout(to_x, to_y, to_x + vw.getWidth(), to_y + vw.getHeight());
        }
        return true;
    }
}