package com.example.android.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private EditText cityInput;
    private TextView temp;
    private TextView cityName;
    private TextView description;
    private TextView tempFeelsLike;
    private TextView windSpeedText;
    private TextView minTemp;
    private TextView maxTemp;
    private TextView country;
    private TextView time;
    private ImageView accept;

    private OkHttpClient client;

    private SharedPreferences mSettings;
    private static final String SAVE_CITY = "city";
    public static final String APP_PREFERENCES = "settings";
    private String city = "";
    public static final String key = "896bb5411d824c6cbd6bfc4a2a165b4d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        accept = findViewById(R.id.accept);
        temp = findViewById(R.id.tempInfo);
        cityInput = findViewById(R.id.inputCity);

        cityName = findViewById(R.id.cityName);
        description = findViewById(R.id.description);
        tempFeelsLike = findViewById(R.id.tempFeelsLike);
        windSpeedText = findViewById(R.id.windSpeedText);
        minTemp = findViewById(R.id.minTemp);
        maxTemp = findViewById(R.id.maxTemp);
        country = findViewById(R.id.country);
        time = findViewById(R.id.time);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        client = new OkHttpClient();

        if (mSettings.contains(SAVE_CITY)) {
            getWeather(mSettings.getString(SAVE_CITY, ""));
        } else
            getWeather("Moscow");

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                city = cityInput.getText().toString();
                getWeather(city);
            }
        });
    }

    private void getWeather(String city) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city
                + "&appid=" + this.key + "&units=metric&lang=ru";
        Request request = new Request.Builder()
                .url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    final String result = response.body().string();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(result);
                                JSONObject details = jsonObject.getJSONArray("weather").getJSONObject(0);
                                cityName.setText(jsonObject.get("name") + "");

                                Long updatedAt = jsonObject.getLong("dt");
                                String updateAtText = "Последнее обновление: "
                                + new SimpleDateFormat("dd/MM/yyyy HH:mm a")
                                        .format(new Date(updatedAt * 1000));
                                time.setText(updateAtText);

                                temp.setText((int) jsonObject.getJSONObject("main")
                                        .getDouble("temp") + "℃");
                                description.setText(details.getString("description")
                                        .substring(0, 1).toUpperCase()
                                        + details.getString("description")
                                        .substring(1));
                                tempFeelsLike.setText((int) jsonObject.getJSONObject("main")
                                        .getDouble("feels_like") + "℃");
                                windSpeedText.setText(jsonObject.getJSONObject("wind")
                                        .get("speed") + " км/ч");
                                minTemp.setText((int)jsonObject.getJSONObject("main")
                                        .getDouble("temp_min") + "℃");
                                maxTemp.setText((int)jsonObject.getJSONObject("main")
                                        .getDouble("temp_max") + "℃");
                                country.setText(jsonObject.getJSONObject("sys")
                                        .get("country") + "");
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "Данные введены некорректно!"
                                        , Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(SAVE_CITY, city).commit();
    }
}