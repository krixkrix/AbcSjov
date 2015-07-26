package dk.kristiannoergaard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;
import android.util.Log;

public class ChallengeFinder {
	
	public ArrayList< ArrayList<Challenge> > challengeMap = new ArrayList< ArrayList<Challenge> > ();
	public ArrayList<Challenge> mLastChallengeSet = null;
	private static final String TAG = "Q1XML";
	private Random random = new Random();
	private int mUsedChallengeMapIndex = -1;
	private int mUsedChallengeIndex = -1;
	
	public ChallengeFinder(){
		xmlScan();
	}

    @SuppressWarnings("unchecked")
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
					// pass
				}
				xrp.next();
			}
            xrp.close();

            mLastChallengeSet = (ArrayList<Challenge>) challenges.clone();

        } catch (XmlPullParserException xppe) {
			Log.e(TAG, "Failure of .getEventType or .next, probably bad file format");
			xppe.toString();
		} catch (IOException ioe) {
			Log.e(TAG, "Unable to read resource file");
			ioe.printStackTrace();
		}
        catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException in scanXml");
            e.printStackTrace();
        }
	}
	
	public boolean removeLatestChallenge(){
		ArrayList<Challenge> challenges = challengeMap.get(mUsedChallengeMapIndex);
		challenges.remove(mUsedChallengeIndex);
		return true;
	}

    @SuppressWarnings("unchecked")
	public Challenge next( int mLevel, int mScore ) {

        if (challengeMap == null){
            xmlScan();
        }

		// if level has increased beyond the defined challenges, pick the last challenge
		if (challengeMap.size() >= mLevel)
			mUsedChallengeMapIndex = mLevel-1;
		else
			mUsedChallengeMapIndex = challengeMap.size()-1;

		ArrayList<Challenge> challenges = challengeMap.get(mUsedChallengeMapIndex);

		if (challenges.size() == 0){
    	    challengeMap.set(mUsedChallengeMapIndex, (ArrayList<Challenge>)  mLastChallengeSet.clone());
			challenges = challengeMap.get(mUsedChallengeMapIndex);
		}

    	mUsedChallengeIndex = random.nextInt(challenges.size());
    	
    	// clone challenge so we can modify the options freely
    	Challenge ch = (Challenge) challenges.get(mUsedChallengeIndex).clone();
    	
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


        if (longOptions != null && longOptions.length() > 0)
            return selectTwoLetterOptions(n, answer, longOptions);

		String vokaler = "AEIOUYÆØÅ";
		char c1 = answer.charAt(1);
		if ( vokaler.indexOf(c1) != -1 ) {
            return generateAutoOptionsVokal(n, answer, avoid);
        } else {
            return generateAutoOptionsConsonant(n, answer, avoid);
		}
	}	
	
	// make 2-letter options with 'vokal' as second letter
	private String generateAutoOptionsVokal(int nOptions, String answer, String avoid) {

        String vokaler = "A,E,I,O,U,Y,Æ,Ø,Å";
        ArrayList<String> vokalerList = new ArrayList<String>(Arrays.asList(vokaler.split(",")));

        String c0 = answer.substring(0, 1);
        String c1 = answer.substring(1,2);
        Collections.shuffle(vokalerList);

        ArrayList<String> options = new ArrayList<String>();
        options.add(answer);

        if (nOptions > vokalerList.size())
            nOptions = vokalerList.size();

        int i = 0;
        while (options.size() < nOptions) {

            // add if different from answer
            if (!c1.equals(vokalerList.get(i)))
                options.add(c0 + vokalerList.get(i));

            i++;
        }

        Collections.shuffle(options);
        return joinArray(options);
    }

	// generate 2-letter options with no 'vokaler'
    private String generateAutoOptionsConsonant(int nOptions, String answer, String avoid){

        char c0 = answer.charAt(0);
        String allOptionsStr;
        switch (c0) {
            case 'B':
                allOptionsStr = "BJ,BL,BR,BV";
                break;
            case 'D':
                allOptionsStr = "DJ,DL,DR,DV";
                break;
            case 'F':
                allOptionsStr = "FJ,FL,FR,FV";
                break;
            case 'G':
                allOptionsStr = "GJ,GL,GR,GV";
                break;
            case 'K':
                allOptionsStr = "KJ,KL,KR,KV";
                break;
            case 'P':
                allOptionsStr = "PJ,PL,PR,PV";
                break;
            case 'S':
                allOptionsStr = "SJ,SK,SL,SM,SN,SP,ST,SV";
                break;
            case 'T':
                allOptionsStr = "TJ,TR,TN,TV";
                break;
            default:
                allOptionsStr = "XX,YY,ZZ";
                break;
        }

        ArrayList<String> allOptions = new ArrayList<String>(Arrays.asList(allOptionsStr.split(",")));
        Collections.shuffle(allOptions);

        ArrayList<String> options = new ArrayList<String>();
        options.add(answer);

        if (nOptions > allOptions.size() )
            nOptions = allOptions.size();

        int i = 0;
        while (options.size() < nOptions) {

            // add if different from answer
            if (!answer.equals(allOptions.get(i)))
                options.add(allOptions.get(i));

            i++;
        }

        Collections.shuffle(options);
        return joinArray(options);

    }

    // select n options from the string allOptionsStr
    // make sure it contains 'answer'
    private String selectTwoLetterOptions(int nOptions, String answer, String allOptionsStr){

        String[] allOptions = allOptionsStr.split(",");

        int n = nOptions;
        if ( n > allOptions.length)
            n = allOptions.length;

        int posAnswer = random.nextInt(n);

        ArrayList<String> options = new ArrayList<String>();
        for (int i=0; i<n; i++){
            if ( i == posAnswer ){
                // input correct answer as option
                options.add(answer);
            }
            else {
                // find a random option different from correct answer
                while ( true ){
                    String candidate = allOptions[ random.nextInt( allOptions.length ) ];
                    if ( candidate.equals(answer))
                        continue;
                    if ( options.contains(candidate) )
                        continue;

                    // new option found
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
    		abc = "ABCDEFGHIJKLMNOPRSTUVYÆØÅ";
    	}
    	else abc = longOptions;

        int nOptions = n;
        if (nOptions > abc.length())
            nOptions = abc.length();

        String options = "";
        int pos = random.nextInt(nOptions);
        for ( int i=0; i<nOptions; i++ ){
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
