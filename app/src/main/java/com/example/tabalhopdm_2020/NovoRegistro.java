package com.example.tabalhopdm_2020;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NovoRegistro extends AppCompatActivity implements LocationListener {

    EditText txtData, txtLocal;
    Button txtRastrear;
    LocationManager locationManager;
    List<Map<String, Object>> lista;
    int registro_id;
    String user_id, user_nome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_registro);

        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#444444")));
        setTitle("Novo registro");

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        user_nome = intent.getStringExtra("user_nome");

        txtData = findViewById(R.id.txt_data);
        SimpleDateFormat formataData = new SimpleDateFormat("dd-MM-yyyy");
        Date data = new Date();
        String dataFormatada = formataData.format(data);
        txtData.setText(dataFormatada);
        txtLocal = findViewById(R.id.txt_local);
        txtRastrear = findViewById(R.id.txtRastrear);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    public void onClickRastreamento(View view) throws ParseException {

        String data = txtData.getText().toString();
        String local = txtLocal.getText().toString();

        if (data.equals("") && local.equals("")) {
            Toast.makeText(getApplicationContext(), "Informe os dados para iniciar o rastreamento!", Toast.LENGTH_SHORT).show();
        } else {
            // convertendo a data p/ salvar
            SimpleDateFormat dateFormatIn = new SimpleDateFormat("dd-MM-yy", Locale.US);
            SimpleDateFormat dateFormatInOut = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date convertedDate = new Date();
            convertedDate = dateFormatIn.parse(data);
            data = String.valueOf(dateFormatInOut.format(convertedDate));

            HttpAsyncTak task = new HttpAsyncTak();
            task.execute(data, local, user_id);
        }
    }

    public void onClickStopRastreamento(View view) {

        for (int i = 0; i < lista.size(); i++) {
            Map<String, Object> dado = lista.get(i);
            double lat = (double) dado.get("lat");
            double lon = (double) dado.get("lon");

            HttpAsyncTaskSend task = new HttpAsyncTaskSend();
            task.execute(lat, lon);
        }

        Intent intent = new Intent(this, ListaRegistros.class);
        intent.putExtra("user_nome", user_nome);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }

    private class HttpAsyncTak extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(NovoRegistro.this);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://appwebaplication.000webhostapp.com/insertNovoRegistro.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                ContentValues values = new ContentValues();
                values.put("txtData", params[0]);
                values.put("txtLocal", params[1]);
                values.put("txtUser", params[2]);
                OutputStream out = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(getFormData(values));
                writer.flush();
                int status = con.getResponseCode();

                if (status == 200) {
                    InputStream stream = new BufferedInputStream(con.getInputStream());
                    BufferedReader buff = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder builder = new StringBuilder();
                    String str = "";
                    while ((str = buff.readLine()) != null) {
                        builder.append(str);
                    }
                    con.disconnect();
                    return builder.toString();
                }
            } catch (Exception e) {
                Log.e("URL", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (result.equals("0")) {
                Toast.makeText(NovoRegistro.this, "Falha ao inserir o registro!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(NovoRegistro.this, "Registro inserido com sucesso!", Toast.LENGTH_SHORT).show();
                registro_id = Integer.parseInt(result);
                txtData.setEnabled(false);
                txtLocal.setEnabled(false);
                txtRastrear.setEnabled(false);

                // iniciando o rastreamento
                long tempo = 0;
                float distancia = 10;
                lista = new ArrayList<>();

                if (ActivityCompat.checkSelfPermission(NovoRegistro.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(NovoRegistro.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(NovoRegistro.this, "Favor permitir uso da localização nas Configurações do dispositivo", Toast.LENGTH_LONG).show();
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, tempo, distancia, (LocationListener) NovoRegistro.this);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, tempo, distancia, (LocationListener) NovoRegistro.this);
            }
        }
    }

    private String getFormData(ContentValues values) {
        try {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, Object> entry : values.valueSet()) {
                if (first)
                    first = false;
                else
                    sb.append("&");
                sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                sb.append("=");
                sb.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double txtLat = location.getLatitude();
        double txtLon = location.getLongitude();
        String txtStatus = location.getProvider();

        Map<String, Object> mapa = new HashMap<>();
        mapa.put("lat", txtLat);
        mapa.put("lon", txtLon);
        lista.add(mapa);

        Toast.makeText(NovoRegistro.this, txtLat + " " + txtLon, Toast.LENGTH_LONG).show();
    }

    private class HttpAsyncTaskSend extends AsyncTask<Double, Void, String> {
        @Override
        protected String doInBackground(Double... params) {
            try {
                URL url = new URL("https://appwebaplication.000webhostapp.com/insertNovoGeolocal.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                ContentValues values = new ContentValues();
                values.put("txtLat", params[0]);
                values.put("txtLon", params[1]);
                values.put("txtRegistro", registro_id);
                OutputStream out = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(getFormData(values));
                writer.flush();
                int status = con.getResponseCode();

                if (status == 200) {
                    InputStream stream = new BufferedInputStream(con.getInputStream());
                    BufferedReader buff = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder builder = new StringBuilder();
                    String str = "";
                    while ((str = buff.readLine()) != null) {
                        builder.append(str);
                    }
                    con.disconnect();
                    return builder.toString();
                }
            } catch (Exception e) {
                Log.e("URL", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result.equals("1")) {
                Toast.makeText(NovoRegistro.this, "Registro inserido com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(NovoRegistro.this, "Falha ao inserir o registro!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
