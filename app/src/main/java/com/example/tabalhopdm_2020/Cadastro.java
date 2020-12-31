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

public class Cadastro extends AppCompatActivity {
    private EditText txt_nome, txt_email, txt_senha;
    String user_nome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#444444")));
        setTitle("Cadastro");

        txt_nome = findViewById(R.id.txt_nome);
        txt_email = findViewById(R.id.txt_email);
        txt_senha = findViewById(R.id.txt_senha);
    }

    public void onClickRegistros(View view) {
        String nome = txt_nome.getText().toString();
        String email = txt_email.getText().toString();
        String senha = txt_senha.getText().toString();
        user_nome = nome;

        if (email.equals("") && senha.equals("") && nome.equals("")) {
            Toast.makeText(getApplicationContext(), "Informe todos os dados para o cadastro!", Toast.LENGTH_SHORT).show();
        } else {
            HttpAsyncTaskSend task = new HttpAsyncTaskSend();
            task.execute(nome, email, senha);
        }
    }

    private class HttpAsyncTaskSend extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Cadastro.this);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("https://appwebaplication.000webhostapp.com/insertNovoCadastro.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                ContentValues values = new ContentValues();
                values.put("nome", params[0]);
                values.put("email", params[1]);
                values.put("senha", params[2]);
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
            if (result.equals("0")) {
                Toast.makeText(Cadastro.this, "Falha ao inserir o registro!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Cadastro.this, "Registro inserido com sucesso!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ListaRegistros.class);
                intent.putExtra("user_nome", user_nome);
                intent.putExtra("user_id", result);
                startActivity(intent);
            }
        }
    }
}