package anuode.apitest;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.anuode.entity.BaseActivity;

/**
 * Created by xudong on 2015/4/9.
 */
public class FlipAdapter extends BaseAdapter {
    private Activity activity;

    public FlipAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            if (position == 0) {
                convertView = LayoutInflater.from(activity).inflate(R.layout.item1, null);
            } else {
                convertView = LayoutInflater.from(activity).inflate(R.layout.flip, null);
            }
        }
        return convertView;
    }
}
