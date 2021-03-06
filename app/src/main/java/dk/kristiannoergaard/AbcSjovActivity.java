package dk.kristiannoergaard;

/*
 * TODO:
 * Loop last challenge
 * Challenges with hard options like k and g, b and p
 * Something special to happen between levels
 */  

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class AbcSjovActivity extends Activity {

	private static Context context;
	
	public static final String PREFS_NAME = "AbcPrefsFile";
	
	// holds the correct answer for the active challenge
	private String mCorrectAnswer = "?";
	
	// holds the current score 0-10
	private int mScore = 0;

	// holds the current level: 1-n
	private int mLevel = 1;

	// the delay between challenges (configurable)
	private int mChallengeDelayMs = 1000;
	
	// flag to indicate when the user can give his answer.
	private boolean mAcceptButtonPress = true;
		
	private ChallengeFinder challengeFinder;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AbcSjovActivity.context = getApplicationContext();
        challengeFinder = new ChallengeFinder();
                      
        mScore = 0;
        mLevel = -1;

        setContentView(R.layout.main);
    }
    
    public static Context getAppContext() {
        return AbcSjovActivity.context;
    }
    
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event)
    {
    	if(keycode == KeyEvent.KEYCODE_MENU)
    	{    
    		Intent settingsActivity = new Intent(getBaseContext(),	Preferences.class);
    		startActivity(settingsActivity);
    	}
    return super.onKeyDown(keycode,event);
    }
    
    @Override
    protected void onStop(){
       super.onStop();

      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putString("levelPref", Integer.toString(mLevel));
      editor.putInt("score", mScore);

      // Commit the edits!
      editor.commit();
    }

    @Override
    protected void onStart() {
    	super.onStart();

		int newLevel;
    	try
        {
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    		String levelStr = prefs.getString("levelPref", "?");
    		newLevel = Integer.parseInt (levelStr);

			mChallengeDelayMs = Integer.parseInt(prefs.getString("challengedelay", "?"));
        }
    	catch(NumberFormatException e)
        {
    		newLevel = 1;
        }

    	if ( newLevel != mLevel){
    		mLevel = newLevel;
    		mScore = 0;
    		updateScoreLevel();
    		setNextChallenge();
    	}
    }


    public void answer(String s){
    	
    	if (!mAcceptButtonPress)
    		return;
    	
    	// Perform action on clicks
    	mAcceptButtonPress = false;
    	boolean correct = s.equals(mCorrectAnswer);

    	popup(correct, mChallengeDelayMs);
    	
    	if ( correct ){
    		mScore++;
    		challengeFinder.removeLatestChallenge();
    	}
    	else {
    		mScore--;
    		if ( mScore < 0 )
    			mScore = 0;
    		
    	}
    	
    	if ( mScore == 10 ){
        	mScore = 0;
        	mLevel++;
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                setNextChallenge();
            }
        }, mChallengeDelayMs + 300);

		updateScoreLevel();

		// debug mode auto answer
        boolean autoanswer = false;
        if (autoanswer) {
            Handler handler2 = new Handler();
            handler2.postDelayed(new Runnable() {
                public void run() {
                    autoAnswer();
                }
            }, mChallengeDelayMs + 500);
        }

	}
    
    void updateScoreLevel(){
    	final RatingBar score = (RatingBar) findViewById(R.id.score);
        score.setRating(mScore);
        final TextView level = (TextView) findViewById(R.id.level);
        level.setText(Integer.toString(mLevel));
    }
    
    void setNextChallenge(){
    	Challenge ch = challengeFinder.next(mLevel, mScore);
    	
        /// store correct answer
        mCorrectAnswer = ch.answer;
    	
    	// find challenge image
    	ImageView myImage = (ImageView) findViewById(R.id.testimage);
    	myImage.setImageResource( ch.resid );
    	
    	// Letter buttons
    	GridView gridview = (GridView) findViewById(R.id.gridview);
    	
    	String[] options = ch.options.split(",");
    	   
    	int nColumns = 3;
    	if ( options.length == 2 || options.length == 4 ){
    		nColumns = 2;
    	}
    	        
    	gridview.setNumColumns(nColumns);
    	gridview.setAdapter(new ButtonAdapter(this, options));
    	mAcceptButtonPress = true;
    }

	/**
	 * @param good: indicates if the popup should display success or failure
	 */
	void popup(boolean good, int delayMs){
    	
    	ImageView smiley = new ImageView(AbcSjovActivity.this);
    	if ( good )
    		smiley.setImageResource( R.drawable.smiley_happy );
    	else
    		smiley.setImageResource( R.drawable.smiley_sad );
    	
    	Toast toast = new Toast(AbcSjovActivity.this);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.setDuration(Toast.LENGTH_SHORT);
    	toast.setView(smiley);
    	    	
    	// Hack
    	final Toast toast2 = toast;
    	toast2.show();
    	    	
    	Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
           public void run() {
               toast2.cancel(); 
           }
        }, delayMs);

    }

    void autoAnswer(){
        answer(mCorrectAnswer);
    }
   
}