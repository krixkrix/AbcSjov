package dk.kristiannoergaard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

/**
 * @author kn
 *
 */

public class ButtonAdapter extends BaseAdapter {
    private AbcSjovActivity mAbcSjovActivity;
    private String[] mOptions;
    
    public ButtonAdapter(AbcSjovActivity a, String[] options) {
        // mContext = c;
        mAbcSjovActivity = a;
        mOptions = options;
    }

    public int getCount() {
        return mOptions.length;
    }
  
    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    	    	
    	OnClickListener onClickListener = new OnClickListener() {
            public void onClick(View v) {
            	String s = (String) v.getTag(); 
            	// Toast.makeText(mAbcSjovActivity, s, Toast.LENGTH_SHORT).show();
            	mAbcSjovActivity.answer(s);
            }
        };
    	
    	Button button;
    	String s = mOptions[position];
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            button = new Button(mAbcSjovActivity);
            int density = mAbcSjovActivity.getResources().getDisplayMetrics().densityDpi;
            int sz = (int)(density/2.6);
            button.setLayoutParams(new GridView.LayoutParams(sz, sz));
            button.setText(s);
            button.setTextColor(Color.rgb(14, 35, 8));
            if ( s.length() == 1)
            	button.setTextSize(40);
            else if ( s.length() == 1)
            	button.setTextSize(30);
            else button.setTextSize(20);
            // TODO calculate needed text space 
            button.setGravity(Gravity.CENTER);
            button.setTypeface(Typeface.DEFAULT_BOLD);
            button.setPadding(8, 1, 8, 8);
            button.setBackgroundResource(R.drawable.button);
            button.setTag(s);
            button.setOnClickListener(onClickListener);
        } else {
            button = (Button) convertView;
        }
            
        return button;
    }
      
}
