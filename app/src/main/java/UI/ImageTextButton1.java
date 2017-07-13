package UI;

/**
 * Created by zhuo on 2017/5/19.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.zhuoh.zheng_map_ble_z.R;

public class ImageTextButton1 extends RelativeLayout {

    private ImageView imgView;
    private TextView  textView;

    public ImageTextButton1(Context context) {
        super(context,null);
    }

    public ImageTextButton1(Context context,AttributeSet attributeSet) {
        super(context, attributeSet);

        LayoutInflater.from(context).inflate(R.layout.img_text_bt, this,true);

        this.imgView = (ImageView)findViewById(R.id.imgview);
        this.textView = (TextView)findViewById(R.id.textview);

        this.setClickable(true);
        this.setFocusable(true);
    }

    public void setImgResource(int resourceID) {
        this.imgView.setImageResource(resourceID);
    }

    public void setText(String text) {
        this.textView.setText(text);
    }

    public void setTextColor(int color) {
        this.textView.setTextColor(color);
    }

    public void setTextSize(float size) {
        this.textView.setTextSize(size);
    }

}
