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
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText email, senha;
    String user_id, user_nome = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#444444")));

        email = findViewById(R.id.txt_email);
        senha = findViewById(R.id.txt_senha);
    }

    public void onClickCadastro(View view) {
        Intent intent = new Intent(this, Cadastro.class);
        startActivity(intent);
    }

    public void onClickLogin(View view) {
        String txt_email = email.getText().toString();
        String txt_senha = senha.getText().toString();

        if (txt_email.equals("") && txt_senha.equals("")) {
            Toast.makeText(getApplicationContext(), "Informe os dados para o login!", Toast.LENGTH_SHORT).show();
        } else {
            HttpAsyncTaskSend task = new HttpAsyncTaskSend();
            task.execute(txt_email, txt_senha);
        }
    }

    private class HttpAsyncTaskSend extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://appwebaplication.000webhostapp.com/consultaLogin.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                ContentValues values = new ContentValues();
                values.put("email", params[0]);
                values.put("senha", params[1]);
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
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (result != null) {
                try {
                    JSONObject res = new JSONObject(result);
                    JSONArray obj = res.getJSONArray("registros");


                    for (int i = 0; i < obj.length(); i++) {
                        JSONObject json = obj.getJSONObject(i);

                        user_id = json.get("id").toString();
                        user_nome = json.get("nome").toString();
                        String email = json.get("email").toString();
                    }

                    if (user_nome.equals("")) {
                        Toast.makeText(getApplicationContext(), "Email e senha incorretos!", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), ListaRegistros.class);
                        intent.putExtra("user_nome", user_nome);
                        intent.putExtra("user_id", user_id);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}