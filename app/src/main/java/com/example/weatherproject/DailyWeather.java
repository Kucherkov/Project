package com.example.weatherproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class DailyWeather extends AppCompatActivity {
    static final private String APP_PREFERENCES = "GeoSetings";
    private String key = "88c297c16bd077e98c5c2076572813ed";
    private SharedPreferences sharedPreferences;
    private TableLayout tl;
    private TableRow[] tr;
    private TextView [] tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_weather);
        tl = (TableLayout) findViewById(R.id.table_weak);
        tr = new TableRow[9];
        tv = new TextView[6];
        sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        GeoShared();
    }

    private void GeoShared() {
        String[] landDt;
        if (sharedPreferences.contains(APP_PREFERENCES)) {
            landDt = sharedPreferences.getString(APP_PREFERENCES, "").split(",");
            setTitle("Прогноз для : " + landDt[2]);
            String url = "https://api.openweathermap.org/data/2.5/onecall?lat=" + landDt[0] + "&lon=" + landDt[1] + "&exclude=minutely,alerts&appid=" +
                    key + "&units=metric&lang=ru";
            new GetData().execute(url);
        }
    }
    private class GetData extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }

                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);
            try {
                JSONObject obj = new JSONObject(res);
                JSONpars(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
    @SuppressLint("SetTextI18n")
    private void JSONpars(JSONObject obj)
    {
        try {
            JSONArray daily = obj.getJSONArray("daily");
                String [] labls = new String[]{
                        "Дата\t","Прогноз\t\t\t","Вероятность\t\t\n\t\tосадков","Т дневная\t","Т ночная\t\t\t","\t\tСкорость\n\t\tветра\t\t\t\t\t\t\t\t"
                };
                tr[0] = new TableRow(this);
               for(int i = 0;i<labls.length;i++)
               {
                   tv[i] = new TextView(this);
                   tv[i].setTextAppearance(this,R.style.TableTextDaily);
                   tv[i].setText(labls[i]);
                   tr[0].addView(tv[i]);
               }
            tl.addView(tr[0], new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
            for (int i = 1;i<tr.length;i++) {
                tr[i] = new TableRow(this);
                tv[0] = new TextView(this);
                tv[0].setText(convertDate(String.valueOf(daily.getJSONObject(i-1).getLong("dt") * 1000),"dd/MM\t\t"));
                tv[1] = new TextView(this);
                tv[1].setTextAppearance(this,R.style.TableTextStyly);
                Set_icon(daily.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getInt("id"),tv[1]);
                tv[2] = new TextView(this);
                tv[2].setText("\t\t"+String.valueOf((int)(daily.getJSONObject(i-1).getDouble("pop")*100))+"%\t\t");
                tv[3] = new TextView(this);
                tv[3].setText(String.valueOf((int)(daily.getJSONObject(i-1).getJSONObject("temp").getDouble("day")))+"\u2103\t\t");
                tv[4] = new TextView(this);
                tv[4].setText(String.valueOf((int)(daily.getJSONObject(i-1).getJSONObject("temp").getDouble("night")))+"\u2103\t\t");
                tv[5] = new TextView(this);
                tv[5].setText(String.valueOf(daily.getJSONObject(i-1).getDouble("wind_speed"))+"M/c");
                for(int j = 0 ; j<tv.length;j++)
                {
                    tv[j].setGravity(Gravity.CENTER);
                    if(j!=1)
                    {
                        tv[j].setTextAppearance(this,R.style.TableTextDaily);
                    }
                    tv[j].setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
                    tr[i].addView(tv[j]);
                }
                tl.addView(tr[i], new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));

            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }
    public static String convertDate(String dateInMilliseconds,String dateFormat) {
        return DateFormat.format(dateFormat, Long.parseLong(dateInMilliseconds)).toString();
    }
    private void Set_icon(int actualId,TextView weather)
    {
        int id = actualId / 100;
            if (actualId == 800) {
                weather.setText(R.string.weather_sunny);
            } else {
                switch (id) {
                    case 2:
                        weather.setText(R.string.weather_thunder);
                        break;
                    case 3:
                        weather.setText(R.string.weather_drizzle);
                        break;
                    case 7:
                        weather.setText(R.string.weather_foggy);
                        break;
                    case 8:
                        weather.setText(R.string.weather_cloudy);
                        break;
                    case 6:
                        weather.setText(R.string.weather_snowy);
                        break;
                    case 5:
                        weather.setText(R.string.weather_rainy);
                        break;
                }
            }
        }
    }
