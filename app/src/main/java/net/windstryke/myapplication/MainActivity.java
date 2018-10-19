package net.windstryke.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.HashMap;
import java.util.ArrayList;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText asteroidID;
    TextView responseView;
    ProgressBar progressBar;
    static final String API_KEY = "uU43L4YtGAi07cIpBkUyAqOCqQC00v4L3zZRXm1Y";
    static final String API_URL = "https://api.nasa.gov/neo/rest/v1/neo/";

    private String TAG = MainActivity.class.getSimpleName();
    private ListView lv;

    ArrayList<HashMap<String, String>> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        responseView = (TextView) findViewById(R.id.responseView);
        asteroidID = (EditText) findViewById(R.id.asteroidID);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Button queryButton = (Button) findViewById(R.id.queryButton);
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RetrieveFeedTask().execute();
            }
        });
    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            responseView.setText("Searching...");
        }


        //Call API to check for result with ID
        protected String doInBackground(Void... urls) {
            String asteroid = asteroidID.getText().toString();
            try {
                URL url = new URL(API_URL + asteroid + "?api_key=" + API_KEY);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        //If API returns positive, pull results from API and parse into JSON Objects
        protected void onPostExecute(String response) {
            if(response != null) {
                progressBar.setVisibility(View.GONE);
                try {
                    //make JSON object
                    JSONObject jsonObj = new JSONObject(response);
                    //print information
                    String asteroidName = jsonObj.get("designation").toString();
                    String asteroidID = jsonObj.get("id").toString();

                    //Get Estimated Diameter Main object
                    JSONObject sizeEst = jsonObj.getJSONObject("estimated_diameter");
                    //Get Estimated Diameter Sub-object
                    JSONObject size = sizeEst.getJSONObject("meters");
                    //Get Estimated Diameter string
                    String asteroidMinSize = size.get("estimated_diameter_min").toString();
                    String asteroidMaxSize = size.get("estimated_diameter_max").toString();

                    //Get Orbital Data Main object
                    JSONObject orbitMain = jsonObj.getJSONObject("orbital_data");
                    //Get Orbital Data strings
                    String firstObserve = orbitMain.get("first_observation_date").toString();
                    String lastObserve = orbitMain.get("last_observation_date").toString();
                    //get Orbital Data Sub-object
                    JSONObject orbitSub = orbitMain.getJSONObject("orbit_class");
                    String orbitClass = orbitSub.get("orbit_class_description").toString();

                    String danger = jsonObj.get("is_potentially_hazardous_asteroid").toString();
                    String information = jsonObj.get("nasa_jpl_url").toString();


                    //Output results
                    responseView.setText("Asteroid Name: " + asteroidName + "\n" +
                                        "ID: " + asteroidID + "\n\n" +
                                        "First Observed: " + firstObserve + "\n" +
                                        "Latest Observation: " +  lastObserve + "\n\n" +
                                        "Est. Diameter: Between " + asteroidMinSize + " and " +
                                        asteroidMaxSize + " Meters\n\n" +
                                        "Orbit Description: " + orbitClass + "\n\n" +
                                        "Potentially Hazardous: " + danger + "\n\n" +
                                        "Additional Information can be found at\n" + information);


                } catch (final JSONException e) {
                    Log.e(TAG, "Error");
                }

            } else {
            Log.e(TAG, "The asteroid does not exist.");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "No JSON to return.",
                            Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
}
