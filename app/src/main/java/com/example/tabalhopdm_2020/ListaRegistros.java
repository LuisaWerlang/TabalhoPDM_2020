package com.example.tabalhopdm_2020;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ListaRegistros extends AppCompatActivity {

    ListView registros;
    String de[] = {"data", "local"};
    int para[] = {R.id.data_registro, R.id.local_registro};
    List<Map<String, String>> lista;
    ArrayList<String> listaLat = new ArrayList<>();
    ArrayList<String> listaLon = new ArrayList<>();
    String registro_data, registro_local, user_id, user_nome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_registros);

        Intent intent = getIntent();
        user_nome = intent.getStringExtra("user_nome");
        user_id = intent.getStringExtra("user_id");

        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#444444")));
        setTitle("Olá "+user_nome);

        registros = findViewById(R.id.txt_ultimos_registros);

        registros.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                registro_data = lista.get(i).get("data");
                registro_local = lista.get(i).get("local");
                new HttpAsyncTak2().execute(lista.get(i).get("id"));
            }
        });

        lista = new ArrayList<>();
        new HttpAsyncTak().execute(user_id);
    }

    public void onClickNovoRegistro(View view) {
        Intent intent = new Intent(this, NovoRegistro.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("user_nome", user_nome);
        startActivity(intent);
    }

    private class HttpAsyncTak extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ListaRegistros.this);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://appwebaplication.000webhostapp.com/registros.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                ContentValues values = new ContentValues();
                values.put("user_id", params[0]);
                OutputStream out = con.getOutputStream();
                BufferedWriter writer =  new BufferedWriter(new OutputStreamWriter(out));
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
            if (result != null) {
                try {
                    JSONObject res = new JSONObject(result);
                    JSONArray obj = res.getJSONArray("registros");
                    SimpleDateFormat dateFormatIn = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    SimpleDateFormat dateFormatInOut = new SimpleDateFormat("dd-MM-yy", Locale.US);
                    Date convertedDate = new Date();

                    for (int i = 0; i < obj.length(); i++) {
                        JSONObject json = obj.getJSONObject(i);

                        String data = json.get("data").toString();
                        String local = json.get("local").toString();
                        String id = json.get("id").toString();

                        convertedDate = dateFormatIn.parse(data);
                        data = String.valueOf(dateFormatInOut.format(convertedDate));

                        Map<String, String> mapa = new HashMap<>();
                        mapa.put("data", data);
                        mapa.put("local", local);
                        mapa.put("id", id);
                        lista.add(mapa);
                    }

                    SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), lista, R.layout.linha_registro, de, para);
                    registros.setAdapter(adapter);

                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private class HttpAsyncTak2 extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://appwebaplication.000webhostapp.com/geolocais.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                ContentValues values = new ContentValues();
                values.put("txtRegistro", params[0]);
                OutputStream out = con.getOutputStream();
                BufferedWriter writer =  new BufferedWriter(new OutputStreamWriter(out));
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
            if (result != null) {
                try {
                    JSONObject res = new JSONObject(result);
                    JSONArray obj = res.getJSONArray("registros");

                    for (int i = 0; i < obj.length(); i++) {
                        JSONObject json = obj.getJSONObject(i);

                        String latitude = json.get("latitude").toString();
                        String longitude = json.get("longitude").toString();

                        listaLat.add(String.valueOf(latitude));
                        listaLon.add(String.valueOf(longitude));
                    }

                    Intent intent = new Intent(getApplicationContext(), Mapa.class);
                    intent.putStringArrayListExtra("lat", listaLat);
                    intent.putStringArrayListExtra("lon", listaLon);
                    intent.putExtra("data", registro_data);
                    intent.putExtra("local", registro_local);
                    startActivity(intent);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}