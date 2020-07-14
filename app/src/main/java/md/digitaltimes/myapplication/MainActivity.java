package md.digitaltimes.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button loadTemp = findViewById(R.id.loadTemp);
        loadTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void getLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    try {
                        setUrl(location);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void setUrl(Location location) throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        String cityName = addresses.get(0).getLocality();
        cityName = cityName.toLowerCase();
        cityName = Normalizer.normalize(cityName, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        Toast.makeText(this, "City: " + cityName, Toast.LENGTH_SHORT).show();

        String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + cityName + "&appid=912ed8064a700fd14d60fb607a1e2aba";
        Toast.makeText(this, "City: " + cityName, Toast.LENGTH_SHORT).show();
        RequestQueue queue = Volley.newRequestQueue(this);

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                JSONArray jsonArray = null;
                String copyData = "";
                DecimalFormat df2 = new DecimalFormat("#.##");
                StringBuilder stringBuilder = new StringBuilder();
                TextView textView = findViewById(R.id.textView);
                try {
                    jsonObject = new JSONObject(response);
                    jsonArray = jsonObject.optJSONArray("list");

                    for (int i = jsonArray.length() - 1; i >= 0; i--) {
                        double temp = Double.parseDouble(jsonArray.getJSONObject(i).getJSONObject("main").get("temp").toString()) - 272.15; // kelvin to celsius 1 kelvin = =272.15 degrees Celsius
                        String dt_txt = jsonArray.getJSONObject(i).getString("dt_txt"); // YYYY-MM-DD HH-MM-SS
                        String[] parts = dt_txt.split(" "); // Splitting String where has space

                        if (!parts[0].equals(copyData)) {
                            copyData = parts[0];
                            stringBuilder.append("\nDate: " + parts[0])
                                    .append("\nDescription: " + getDescription(jsonArray.getJSONObject(i)))
                                    .append("\nTemp: " + df2.format(temp) + "C\n");
                        }
                    }
                    textView.setVisibility(0);
                    textView.setText(stringBuilder.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("That didn't work!");
            }
        });
        queue.add(stringRequest);
    }

    private String getDescription(JSONObject jsonArray) throws JSONException {
        JSONArray arr = jsonArray.optJSONArray("weather");
        String description = arr.getJSONObject(0).getString("description");

        return description;
    }
}
