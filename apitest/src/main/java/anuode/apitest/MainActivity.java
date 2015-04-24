package anuode.apitest;

import android.graphics.drawable.ClipDrawable;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.widget.ImageView;

import com.anuode.entity.ANuodeBaseActivity;
import com.anuode.util.Logs;

import anuode.apitest.flipview.FlipView;
import anuode.apitest.flipview.OverFlipMode;

public class MainActivity extends ANuodeBaseActivity implements FlipView.OnFlipListener, FlipView.OnOverFlipListener,FlipAdapter1.Callback{
    private FlipView mFlipView;
    private FlipAdapter1 mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mFlipView = (FlipView) findViewById(R.id.flip_view);
//        mAdapter = new FlipAdapter1(this);
//        mAdapter.setCallback(this);
//        mFlipView.setAdapter(mAdapter);
//        mFlipView.setOnFlipListener(this);
////        mFlipView.peakNext(false);
////        mFlipView.setOverFlipMode(OverFlipMode.GLOW);
//        mFlipView.setEmptyView(findViewById(R.id.empty_view));
//        mFlipView.setOnOverFlipListener(this);



//        KennerControll controll = (KennerControll)App.app.cache.get("KennerControll");
//        Task task = new Task(0,ApiTest.class,"TestLogin",null,null);
//        task.activity = this;
////        task.serializableClass =Login.class;
//        controll.doTask(task);
        ImageView img = (ImageView)findViewById(R.id.img);
//R.drawable.trippackage__hotle

        ClipDrawable drawable = new ClipDrawable( getResources().getDrawable(R.drawable.trippackage__hotle), Gravity.CENTER, 2);
        img.setImageDrawable(drawable);
//        drawable.newDrawable();
//        drawable.setLevel(5000);
//        img.setImageDrawable(drawable.new);
        Logs.w("level:",img.getDrawable() + " ..  " + img.getDrawable().getLevel() +" ..");
//        img.getDrawable().setLevel(5000);
//        SimpleDraweeView view = (SimpleDraweeView) findViewById(R.id.img);
////        view.setImageURI(Uri.parse("http://pic1.ooopic.com/uploadfilepic/sheji/2009-08-05/OOOPIC_SHIJUNHONG_20090805326b1a80e6c6aa6a.jpg"));
//        view.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);
//        view.getDrawable().setLevel(5000);
    }


    @Override
    public void freash(Object object) {
        super.freash(object);
        Message msg = (Message) object;
//        Login login = (Login)((Message)object).obj;
        Logs.w("freash",object.toString() +"  ");
    }

    @Override
    public void onFlippedToPage(FlipView v, int position, long id) {

    }

    @Override
    public void onOverFlip(FlipView v, OverFlipMode mode, boolean overFlippingPrevious, float overFlipDistance, float flipDistancePerPage) {

    }

    @Override
    public void onPageRequested(int page) {

    }
}
