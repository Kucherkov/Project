package com.example.weatherproject;


import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


public class MainActivity extends AppCompatActivity {
    //
    private TextView weather;
    private TextView humidity;
    private TextView wind_speed;
    private TextView pressure;
    private TextView temper;
    private Button weak_weathe;
    private TableLayout tl;
    private AlarmManager am;
    private TableRow[] tr;
    private TextView[] tv;
    private Button google_out;
    private SignInButton google_in;
    //
    Intent intent;
    private String key = "88c297c16bd077e98c5c2076572813ed";
    private String [] landDt;
    String url = null;
    static final private int CHOOSE_THIEF = 0;
    private int RC_SIGN_IN = 1;
    static final private String APP_PREFERENCES = "GeoSetings";
    private SharedPreferences sharedPreferences;
    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;
    private LoginButton loginButton;
    private FirebaseAuth.AuthStateListener authStateListener;
    private AccessTokenTracker accessTokenTracker;
    private GoogleSignInClient googleSignInClient;

    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_design);
        weather = findViewById(R.id.weather_icon);
        humidity = findViewById(R.id.humidity_txt);
        pressure = findViewById(R.id.pressure_txt);
        wind_speed = findViewById(R.id.wind_speed);
        temper = findViewById(R.id.temp);
        weak_weathe = findViewById(R.id.weak_button);
        tl = (TableLayout) findViewById(R.id.table);
        sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        //
        google_in = findViewById(R.id.google_in);
        google_out = findViewById(R.id.google_out);
        firebaseAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email","public_profile");
        callbackManager = CallbackManager.Factory.create();
        //
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this,gso);
        google_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        google_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignInClient.signOut();
                Toast.makeText(MainActivity.this,"Logged out",Toast.LENGTH_SHORT).show();
                google_out.setVisibility(View.INVISIBLE);
            }
        });
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                FacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null)
                {
                    updateUser(user);
                }
            }
        };
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken==null)
                {
                    firebaseAuth.signOut();
                }
            }
        };
        setTitle("Выберите пункт с меню");
        TableInit();
        weak_weathe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this,DailyWeather.class);
                startActivityForResult(intent,CHOOSE_THIEF);
            }
        });
        GeoShared();
    }
    private  void  FacebookToken(AccessToken accessToken)
    {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    updateUser(user);
                }
            }
        });
    }
    private void updateUser(FirebaseUser user)
    {
        if(user!=null)
        {
            Toast.makeText(MainActivity.this,user.getDisplayName(),Toast.LENGTH_SHORT).show();
        }

    }
    private void signIn()
    {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }
    private void googleSignInResult(Task<GoogleSignInAccount> completedTask)
    {
        try {
            GoogleSignInAccount asc = completedTask.getResult(ApiException.class);
            FirebaseGoogleAuth(asc);
        }
        catch (ApiException e){}

    }
    private void FirebaseGoogleAuth(GoogleSignInAccount acct)
    {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                    }
                });
    }
    private void updateUI(FirebaseUser fuser)
    {
        google_out.setVisibility(View.VISIBLE);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account!=null)
        {
            String name = account.getGivenName();
            String Email = account.getEmail();
            Toast.makeText(MainActivity.this,name+Email,Toast.LENGTH_SHORT).show();
        }
    }
    //Create Table for hourly forecast
    private void TableInit()
    {
        tr = new TableRow[3];
        tv = new  TextView[24];
    }

    // Check GeoPreferences and Internet Connection
    private void GeoShared()
    {
        if (sharedPreferences.contains(APP_PREFERENCES)&& checkInternetConnection()==true) {
            landDt = sharedPreferences.getString(APP_PREFERENCES,"").split(",");
            setTitle("Прогноз для : "+landDt[2]);
            url = "https://api.openweathermap.org/data/2.5/onecall?lat="+landDt[0]+"&lon="+landDt[1]+"&exclude=minutely,alerts&appid="+key+"&units=metric&lang=ru";
            new GetURLData().execute(url);
        }
    }

    // Get answers from CityActivity and MapActivity and create URL request
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_THIEF) {
            if (resultCode == RESULT_OK) {
                String landData = data.getStringExtra(MapActivity.THIEF);
                landDt = landData.split(",");
                url = "https://api.openweathermap.org/data/2.5/onecall?lat="+landDt[0]+"&lon="+landDt[1]+"&exclude=minutely,alerts&appid="+key+"&units=metric&lang=ru";
                setTitle("Прогноз для : "+landDt[2]);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(APP_PREFERENCES,landData);
                editor.apply();
                tl.removeAllViews();
                new GetURLData().execute(url);
            }
        }
        if(requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task= GoogleSignIn.getSignedInAccountFromIntent(data);
            googleSignInResult(task);
        }
        callbackManager.onActivityResult(requestCode,resultCode,data);

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener!=null)
        {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // test for connection
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.name_search :
                intent = new Intent(MainActivity.this,CityActivity.class);
                startActivityForResult(intent,CHOOSE_THIEF);
                return true;
            case R.id.map_search:
                intent = new Intent(MainActivity.this,MapActivity.class);
                startActivityForResult(intent,CHOOSE_THIEF);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class  GetURLData extends AsyncTask<String,String,String> {

        protected void onPreExecute(){
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
                while ((line = reader.readLine())!=null)
                {
                    buffer.append(line).append("\n");
                }
                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally{
                if(connection != null)
                {
                    connection.disconnect();
                }

                try {
                    if(reader != null) {
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
        protected  void onPostExecute(String res)
        {
            super.onPostExecute(res);
            try {

                JSONObject obj = new JSONObject(res);
                JSONpars(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    //Parse JSON answer from OpenWeather
    @SuppressLint("SetTextI18n")
    private void JSONpars(JSONObject obj)
    {
        try {
            JSONArray hourly = obj.getJSONArray("hourly");
            JSONArray daily = obj.getJSONArray("daily");
            wind_speed.setText("Cкорость ветра : " +
                    String.valueOf(obj.getJSONObject("current").getDouble("wind_speed")) + " M/C");
            temper.setText(String.valueOf((int) obj.getJSONObject("current").getDouble("temp"))+"\u2103");
            pressure.setText("Давление : " + String.valueOf(obj.getJSONObject("current").getInt("pressure")) + " hPa");
            humidity.setText("Влажность : " + String.valueOf(obj.getJSONObject("current").getInt("humidity")) + " % ");
            Set_icon(obj.getJSONObject("current").getJSONArray("weather").getJSONObject(0).getInt("id"),
                    obj.getJSONObject("current").getLong("dt")*1000,
                    obj.getJSONObject("current").getLong("sunrise")*1000,
                    obj.getJSONObject("current").getLong("sunset")*1000, weather);
            /* Create a new row to be added. */
            for(int i = 0;i<tr.length;i++)
            {
                tr[i] = new TableRow(this);
                tr[i].setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                tr[i].setGravity(Gravity.CENTER);
                if(i == 0)
                {
                    for(int j = 0;j<tv.length;j++)
                    {
                        tv[j] = new TextView(this);
                        tv[j].setTextAppearance(this, R.style.TableText);
                        tv[j].setGravity(Gravity.CENTER);
                        tv[j].setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            tv[j].setText(convertDate(hourly.getJSONObject(j).getLong("dt")*1000,
                                    obj.getString("timezone"))+":00");
                        }
                        tr[i].addView(tv[j]);
                    }
                }
                else if(i==1){
                    int k = 1;
                    for (int j = 0; j < tv.length; j++) {
                        tv[j] = new TextView(this);
                        tv[j].setGravity(Gravity.CENTER);
                        tv[j].setPadding(20,5,20,10);
                        tv[j].setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                        tv[j].setTextAppearance(this, R.style.TableTextStyly);
                        if(daily.getJSONObject(k).getLong("sunrise")>= hourly.getJSONObject(j).getLong("dt")) {
                            Set_icon(hourly.getJSONObject(j).getJSONArray("weather").getJSONObject(0).getInt("id"),
                                    hourly.getJSONObject(j).getLong("dt"),
                                    daily.getJSONObject(k-1).getLong("sunrise"),
                                    daily.getJSONObject(k-1).getLong("sunset"), tv[j]);
                            tr[i].addView(tv[j]);
                        }
                        else
                        {
                            k+=1;
                            Set_icon(hourly.getJSONObject(j).getJSONArray("weather").getJSONObject(0).getInt("id"),
                                    hourly.getJSONObject(j).getLong("dt"),
                                    daily.getJSONObject(k-1).getLong("sunrise"),
                                    daily.getJSONObject(k-1).getLong("sunset"), tv[j]);
                            tr[i].addView(tv[j]);
                        }
                    }
                }
                else
                {
                    for(int j = 0;j<tv.length;j++)
                    {
                        tv[j] = new TextView(this);
                        tv[j].setTextAppearance(this, R.style.TableText);
                        tv[j].setGravity(Gravity.CENTER);
                        tv[j].setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                        tv[j].setText(String.valueOf((int) obj.getJSONArray("hourly").getJSONObject(j + 1).getDouble("temp"))+"\u2103");
                        tr[i].addView(tv[j]);
                    }
                }
                tl.addView(tr[i], new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }
    //Convert Date for hourly format
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String convertDate(Long dateInMilliseconds, String TimeZone) {
        return Instant.ofEpochMilli( dateInMilliseconds )
                .atZone( ZoneId.of( TimeZone ) )
                .toLocalTime()
                .format(
                        DateTimeFormatter.ofPattern( "hh" )
                )     ;
    }
    // Set weather icon
    private void Set_icon(int actualId,long dt,long sunrise,long sunset,TextView weather)
    {
        int id = actualId / 100;
        if (dt>=sunrise && dt <= sunset) {
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
        else
        {
            if(actualId == 800){
                weather.setText(R.string.weather_clear_night);
            } else {
                switch(id) {
                    case 2 :  weather.setText(R.string.weather_thunder_night);
                        break;
                    case 3 :  weather.setText(R.string.weather_drizzle_night);
                        break;
                    case 7 :  weather.setText(R.string.weather_foggy_night);
                        break;
                    case 8 :  weather.setText(R.string.weather_cloudy_night);
                        break;
                    case 6 : weather.setText(R.string.weather_snowy_night);
                        break;
                    case 5 :  weather.setText(R.string.weather_rainy_night);
                        break;
                }
            }
        }

    }


}
