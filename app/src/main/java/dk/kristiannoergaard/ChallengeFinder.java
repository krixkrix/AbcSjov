package dk.kristiannoergaard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;
import android.util.Log;

public class ChallengeFinder {
	
	public ArrayList< ArrayList<Challenge> > challengeMap = new ArrayList< ArrayList<Challenge> > ();

	private static final String TAG = "Q1XML";
	private Random random = new Random();
	private int mChallengeLevel = -1;
	private int mChallengeIndex = -1;
	
	public ChallengeFinder(){
		xmlScan();
	}
	
	private void xmlScan(){

		// the images can be scaled to 400px width by:
		// mkdir results
		// for f in *jpg; do echo "file $f"; convert "$f[400x>]" results/$f; done

		// parse the XML data for challenges
		// Get the Android-specific compiled XML parser.
		ArrayList<Challenge> challenges = null;
        int nOptions = 3;

		try {
			XmlResourceParser xrp = AbcSjovActivity.getAppContext().getResources().getXml(R.xml.q1);

			while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
				if (xrp.getEventType() == XmlResourceParser.START_TAG) {
					String s = xrp.getName();

					if (s.equals("level")) {
						challenges = new ArrayList<Challenge>();
                        String optionStr = xrp.getAttributeValue(null, "options");
                        nOptions = Integer.parseInt(optionStr);
					}
					else if (s.equals("question") && challenges != null ) {

						Challenge ch = new Challenge();
						ch.resid = xrp.getAttributeResourceValue(null, "id", 0);
						ch.answer = xrp.getAttributeValue(null, "answer");
						ch.avoid = xrp.getAttributeValue(null, "avoid");
						ch.options = xrp.getAttributeValue(null, "options");
                        ch.nOptions = nOptions;

						challenges.add(ch);
					}
				} else if (xrp.getEventType() == XmlResourceParser.END_TAG) {
					String s = xrp.getName();
					if (s.equals("level")) {
						challengeMap.add(challenges);
					}
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
		ArrayList<Challenge> challenges = challengeMap.get(mChallengeLevel-1);
		challenges.remove(mChallengeIndex);
		return true;
	}
	
	public Challenge next( int mLevel, int mScore ) {

        if (challengeMap == null){
            xmlScan();
        }

        mChallengeLevel = mLevel;

        ArrayList<Challenge> challenges = challengeMap.get(mLevel-1);
    	int max = challenges.size();

    	mChallengeIndex = random.nextInt(max);
    	
    	// clone challenge so we can modify the options freely
    	Challenge ch = (Challenge) challenges.get(mChallengeIndex).clone();
    	
    	ch.options = makeOptions( ch.nOptions, ch.answer, ch.options, ch.avoid );
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
