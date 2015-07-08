package dk.kristiannoergaard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;
import android.util.Log;

public class ChallengeFinder {
	
	private Map<String, ArrayList<Challenge> > challengeMap = new HashMap<String, ArrayList<Challenge> >();
	private static final String TAG = "Q1XML";
	private Random random = new Random();
	private String mChallengeTag = new String();
	private int mChallengeId = -1;
	
	public ChallengeFinder(){
		xmlScan();
	}
	
	private void xmlScan(){
		
		// init map
		challengeMap.clear();
		challengeMap.put("questions_ks", new ArrayList<Challenge>());
		challengeMap.put("questions_bft", new ArrayList<Challenge>());
		challengeMap.put("firstLetter1", new ArrayList<Challenge>());
		challengeMap.put("firstLetter2", new ArrayList<Challenge>());
		challengeMap.put("firstLetter3", new ArrayList<Challenge>());
		challengeMap.put("doubleLetter1", new ArrayList<Challenge>());
		challengeMap.put("doubleLetter2", new ArrayList<Challenge>());
		
		// parse the XML data for challenges
		// Get the Android-specific compiled XML parser.
		ArrayList<Challenge> challenges = null;
		
		try {
			XmlResourceParser xrp = AbcSjovActivity.getAppContext().getResources().getXml(R.xml.q1);

			while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
				if (xrp.getEventType() == XmlResourceParser.START_TAG) {
					String s = xrp.getName();

					if ( challengeMap.containsKey( s ) ){
						// s was firstLetter1 or similar
						challenges = challengeMap.get(s);
					}
					else if (s.equals("question") && challenges != null ) {

						Challenge ch = new Challenge();
						ch.resid = xrp.getAttributeResourceValue(null, "id", 0);
						ch.answer = xrp.getAttributeValue(null, "answer");
						ch.avoid = xrp.getAttributeValue(null, "avoid");
						ch.options = xrp.getAttributeValue(null, "options");

						challenges.add(ch);
					}
				} else if (xrp.getEventType() == XmlResourceParser.END_TAG) {
					;
				} else if (xrp.getEventType() == XmlResourceParser.TEXT) {
					;
				}
				xrp.next();
			}
            xrp.close();
		} catch (XmlPullParserException xppe) {
			Log.e(TAG, "Failure of .getEventType or .next, probably bad file format");
			xppe.toString();
		} catch (IOException ioe) {
			Log.e(TAG, "Unable to read resource file");
			ioe.printStackTrace();
		}
	}
	
	public boolean removeLatestChallenge(){
		ArrayList<Challenge> challenges = challengeMap.get(mChallengeTag);
		challenges.remove(mChallengeId);
		return true;
	}
	
	public Challenge next( int mLevel, int mScore ){
		
    	int nOptions = 2;
    	mChallengeTag = "";
    	
    	if (mLevel == 1){
    		mChallengeTag = "questions_ks";
    		nOptions = 2;
    	}
    	else if (mLevel == 2){
    		mChallengeTag = "questions_bft";
    		nOptions = 2;
    	}
    	else if (mLevel == 3){
    		mChallengeTag = "questions_ks";
    		nOptions = 4;
    	}
    	else if (mLevel == 4){
    		mChallengeTag = "questions_bf";
    		nOptions = 4;
    	}
    	else if (mLevel == 5 ){
    		mChallengeTag = "firstLetter1";
    		nOptions = 3;
    	}
    	else if (mLevel == 6 ){
    		mChallengeTag = "firstLetter1";
    		nOptions = 6;
    	}
    	else if (mLevel == 7 ){
    		mChallengeTag = "firstLetter1";
    		nOptions = 9;
    	}
    	else if (mLevel<10){
    		mChallengeTag = "firstLetter2";
    		switch (mLevel){
    		case 6: nOptions = 6; break;
    		case 7: nOptions = 7; break;
    		case 8: nOptions = 8; break;
    		case 9: nOptions = 9; break;
    		default:nOptions = 9; break;
    		}
    	}
    	else if (mLevel<16){
    		mChallengeTag = "doubleLetter1";
    		switch (mLevel){
    		case 10: nOptions = 2; break;
    		case 11: nOptions = 3; break;
    		case 12: nOptions = 4; break;
    		case 13: nOptions = 6; break;
    		case 14: nOptions = 8; break;
    		case 15: nOptions = 9; break;
    		default:nOptions = 9; break;
    		}
    	}
    	else if (mLevel<20){
    		mChallengeTag = "doubleLetter2";
    		switch (mLevel){
    		case 16: nOptions = 2; break;
    		case 17: nOptions = 3; break;
    		case 18: nOptions = 4; break;
    		case 19: nOptions = 6; break;
    		case 20: nOptions = 9; break;
    		default:nOptions = 9; break;
    		}
    	}
    	else {
    		if (mLevel%2==0)
    			mChallengeTag = "doubleLetter2";
    		else
    			mChallengeTag = "doubleLetter1";
    		
    		nOptions = 9;
    	}
    	
    	ArrayList<Challenge> challenges = challengeMap.get(mChallengeTag);
    	int max = challenges.size();
    	if ( max == 0 ){
    		xmlScan();
    		challenges = challengeMap.get(mChallengeTag);
        	max = challenges.size();
    	}
    	
    	mChallengeId = random.nextInt(max);
    	
    	// clone challenge so we can modify the options freely
    	Challenge ch = (Challenge) challenges.get(mChallengeId).clone();
    	
    	ch.options = makeOptions( nOptions, ch.answer, ch.options, ch.avoid );
    	return ch;
	}
	
	private String makeOptions(int n, String answer, String longOptions, String avoid ){
		if ( answer.length() == 1 ){
			return makeOneLetterOptions(n, answer, longOptions, avoid);
		}
		else if ( answer.length() == 2 ){
			return makeTwoLetterOptions(n, answer, longOptions, avoid);
		} 
		else {
			Log.e("makeOptions", "Unable to make options");
			 //ioe.printStackTrace();
			return "ERROR";
		}
	}
	
	private String makeTwoLetterOptions(int n, String answer, String longOptions, String avoid){
		String vokaler = "AEIOUYÆØÅ";
		char c1 = answer.charAt(1);
		if ( vokaler.indexOf(c1) != -1 ) {
			return makeTwoLetterOptions1(n, answer, longOptions, avoid);
		}
		else {
			return makeTwoLetterOptions2(n, answer, longOptions, avoid);
		}
	}	
	
	// make 2-letter options with 'vokal' as second letter
	private String makeTwoLetterOptions1(int n, String answer, String longOptions, String avoid){
		String vokaler = "AEIOUYÆØÅ";
		
		char c1 = answer.charAt(1);
		int pos = random.nextInt(n);
		ArrayList<String> options = new ArrayList<String>(); 
		for (int i=0; i<n; i++){
			if ( i == pos ){
				// input correct answer as option
				options.add(answer);
			}
			else {
				// find a random 'vokal' different from correct answer
				while ( true ){
					char a = vokaler.charAt( random.nextInt( vokaler.length() ));
					if ( a == c1 )
						continue;
					String candidate = answer.substring(0, 1) + Character.toString(a);
					if ( options.contains(candidate) ) 
						continue;
					// new character found
					options.add(candidate);
					break;
				}
			}
		}

		return joinArray( options );
	}
		
	// make 2-letter options with no 'vokaler' 
	private String makeTwoLetterOptions2(int n, String answer, String longOptions, String avoid){
		// which letters can follow 
		String bOptions = "JLR";
		String dOptions = "R";
		String fOptions = "JLR";
		String gOptions = "JLR";
		String kOptions = "JLR";
		String pOptions = "JLR";
		String sOptions = "JKLMNPTV";
		String tOptions = "JR";
		char c0 = answer.charAt(0);
		char c1 = answer.charAt(1);
		
		String allOptions;
		switch(c0){
		case 'B': allOptions = bOptions; break;
		case 'D': allOptions = dOptions; break;
		case 'F': allOptions = fOptions; break;
		case 'G': allOptions = gOptions; break;
		case 'K': allOptions = kOptions; break;
		case 'P': allOptions = pOptions; break;
		case 'S': allOptions = sOptions; break;
		case 'T': allOptions = tOptions; break;
		default: allOptions = "XXXXX"; break;
		}
		
		if ( n > allOptions.length())
			n = allOptions.length();
				
		int pos = random.nextInt(n);
		ArrayList<String> options = new ArrayList<String>(); 
		for (int i=0; i<n; i++){
			if ( i == pos ){
	       		// input correct answer as option
	       		options.add(answer);
	       	}
	      	else {
	       		// find a random 'vokal' different from correct answer
	       		while ( true ){
	       			char a = allOptions.charAt( random.nextInt( allOptions.length() ));
	       			if ( a == c1 )
	        			continue;
	        		String candidate = answer.substring(0, 1) + Character.toString(a);
	        		if ( options.contains(candidate) ) 
	        			continue;
	        		// new character found
	        		options.add(candidate);
	        		break;
	        	}
	        }
		}
			
		return joinArray( options );
	}
		
	private String joinArray(ArrayList<String> list){
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<list.size(); i++){
			sb.append(list.get(i));
			if (i!=list.size()-1){
				sb.append(",");
			}
		}
		return sb.toString();
	}
	
	private String makeOneLetterOptions(int n, String answer, String longOptions, String avoid){
	
		String abc;
    	if ( longOptions == null || longOptions.length() == 0 ){
    		abc = new String("ABCDEFGHIJKLMNOPRSTUVYÆØÅ");
    	}
    	else abc = longOptions;
    	
        String options = "";
        int pos = random.nextInt(n);
        for ( int i=0; i<n; i++ ){
        	if ( i == pos ){
        		// input correct answer as option
        		options += answer;
        	}
        	else {
        		// find a random character different from correct answer
        		while ( true ){
        			char a = abc.charAt( random.nextInt( abc.length() ));
        			if ( a == answer.charAt(0) )
        				continue;
        			if ( options.indexOf(a, 0) >= 0 )
        				continue;
        			// new character found
        			options += Character.toString(a);
        			break;
        		}
        	}
        	if ( i!=n)
        		options += ",";
        }
        return options;
    }
    

}
