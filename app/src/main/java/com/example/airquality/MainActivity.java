package com.example.airquality;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    TextView tvVille, tvResulIqa, tvResultPm10, tvResultPm25;
    Button btIqa;
    LocationManager locationManager;
    protected String token;


    private static Context context;
    public static Context getContext() {
        return MainActivity.context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvVille= findViewById(R.id.tvVille);
        tvResulIqa= findViewById(R.id.tvResutIqa);
        tvResultPm25 = findViewById(R.id.tvResulPm25);
        tvResultPm10 = findViewById(R.id.tvResutPm10);
        btIqa = findViewById(R.id.btIqa);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        token="9220c60b347d9d101b466eee7fe67654fb7a45c1";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                }
        }
    }

    public void button(View view) {

        //récupération des coordonnées GPS
        GpsConnect g = new GpsConnect(getApplicationContext());
        Location l = g.getLocation();
        if (l != null) {
            double lat = l.getLatitude();
            double lon = l.getLongitude();

            //connection à l'API
            HttpClient client = new HttpClient("https://api.waqi.info/feed/geo:" + lat + ";" + lon + "/?token=" + token);

            client.start();

            try {
                client.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //récupération du JSON, tranformation et affichage dans les textView
            String reponse = client.getResponse();

            try {
                JSONObject json = new JSONObject(reponse);
                JSONObject reponse2 = json.getJSONObject("data");
                JSONObject resultat = reponse2.getJSONObject("city");
                tvVille.setText(resultat.getString("name"));

                int aqiResultI = reponse2.getInt("aqi");
                String aqiResultS = String.valueOf(aqiResultI);
                tvResulIqa.setText(aqiResultS);
                if(aqiResultI<51){
                    tvResulIqa.setBackgroundColor(getResources().getColor(R.color.green));
                }else if(aqiResultI<101){
                    tvResulIqa.setBackgroundColor(getResources().getColor(R.color.yellow));
                }else if(aqiResultI<151){
                    tvResulIqa.setBackgroundColor(getResources().getColor(R.color.orange));
                }else if(aqiResultI<201){
                    tvResulIqa.setBackgroundColor(getResources().getColor(R.color.red));
                    tvResulIqa.setTextColor(getResources().getColor(R.color.white));
                }else if(aqiResultI<301){
                    tvResulIqa.setBackgroundColor(getResources().getColor(R.color.purple));
                    tvResulIqa.setTextColor(getResources().getColor(R.color.white));
                }else if(aqiResultI>300){
                    tvResulIqa.setBackgroundColor(getResources().getColor(R.color.darkPurple));
                    tvResulIqa.setTextColor(getResources().getColor(R.color.white));
                }

                JSONObject resultatIaqi = reponse2.getJSONObject("iaqi");

                JSONObject resultIaqiPm10 = resultatIaqi.getJSONObject("pm10");
                if(resultIaqiPm10 != null) {
                    double pm10ResultD = resultIaqiPm10.getDouble("v");
                    String pm10ResultS = String.valueOf(pm10ResultD);
                    tvResultPm10.setText(pm10ResultS);
                } else{
                    String pm10Null = "---";
                    tvResultPm10.setText(pm10Null);
                }

                JSONObject  resultIaqiPm25= resultatIaqi.getJSONObject("pm25");
                if(resultIaqiPm25 != null) {
                    double pm25ResultD = resultIaqiPm25.getDouble("v");
                    String pm25ResultS = String.valueOf(pm25ResultD);
                    tvResultPm25.setText(pm25ResultS);
                }else{
                    String pm25Error = "---";
                    tvResultPm25.setText(pm25Error);
                }
            } catch (JSONException e) {
                Toast.makeText(this, "donnée manquante", Toast.LENGTH_LONG).show();
            }

        }
    }
}
