package com.example.tablerodecontrol;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.List;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Tablerodecontrol extends AppCompatActivity {

    String Nombre_Directorio = "MisPDFs";
    String Nombre_Documento = "MiPDF.pdf";
    EditText etTexto;
    Button btnGenerar;
    EditText ETt;
    ListView L3;
    Button bt1,bt2;
    String[] elemento;


    String idM="", Cdm="", Nm="", Ub="", Es="";

    JSONArray ja1 = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tablerodecontrol);

        etTexto = findViewById(R.id.etTexto);
        btnGenerar = findViewById(R.id.btnGenerar);
        L3 = findViewById(R.id.lista3);

        // Permisos
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},
                    1000);
        }

        // Genera el documento

        btnGenerar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearPDF();
                Toast.makeText(Tablerodecontrol.this, "Se creó el PDF", Toast.LENGTH_LONG).show();
            }
        });



        ETt=(EditText)findViewById(R.id.Et1);
        L3=(ListView)findViewById(R.id.lista3);
        bt1=(Button)findViewById(R.id.B1);
        bt2=(Button)findViewById(R.id.B2);

        bt1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {Consultar();
            }
        });


        bt2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {Volver();
            }
        });

    }

    public void crearPDF() {
            Document documento = new Document();

            try {
                File file = crearFichero(Nombre_Documento);
                FileOutputStream ficheroPDF = new FileOutputStream(file.getAbsolutePath());

                PdfWriter writer = PdfWriter.getInstance(documento, ficheroPDF);

                documento.open();

                documento.add(new Paragraph("TABLA \n\n"));
                documento.add(new Paragraph( etTexto.getText().toString()+"\n\n"));

                // Insertamos una tabla
                PdfPTable tabla = new PdfPTable(5);
                for(int i = 0 ; i < 15 ; i++) {
                    tabla.addCell("CELDA "+i);
                }

                documento.add(tabla);

            } catch(DocumentException e) {
            } catch(IOException e) {
            } finally {
                documento.close();
            }
        }

        public File crearFichero(String nombreFichero) {
            File ruta = getRuta();

            File fichero = null;
            if(ruta != null) {
                fichero = new File(ruta, nombreFichero);
            }

            return fichero;
        }

        public File getRuta() {
            File ruta = null;

            if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                ruta = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Nombre_Directorio);

                if(ruta != null) {
                    if(!ruta.mkdirs()) {
                        if(!ruta.exists()) {
                            return null;
                        }
                    }
                }

            }
            return ruta;
        }


    public void Consultar(){
        if(ETt.getText().toString().isEmpty()){//Si estan vacios
            Toast.makeText(getApplicationContext(), "Datos incorrectos",
                    Toast.LENGTH_SHORT).show();
        }
        else{

            new ConsultarDatos().execute("http://10.0.2.2/proyectodegrado/Tablerodecontrol_A.php?id="+ETt.getText().toString());

        }
    }
    public void Volver(){

    }

    public void mostrar(){
        //ArrayList<String> miList = new ArrayList<String>();
        //miList.addAll( Arrays.asList(elementos) );

        ArrayAdapter<String> adapter2=new ArrayAdapter(Tablerodecontrol.this,android.R.layout.simple_list_item_1,elemento);
        L3.setAdapter(adapter2);

        L3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });


    }

    private class ConsultarDatos extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
// params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                ja1 = new JSONArray(result);
                elemento=new String[ja1.length()];

                //id.setText(ja.getString(0));// Aqui imprimetodo el arreglo asociativo
                if(ja1.length()!=0) {
                    for (int i = 0; i < ja1.length(); i++) {
                        JSONObject obj = ja1.getJSONObject(i);

                        idM= obj.getString("idMedicamento");
                        Cdm= obj.getString("codigoMedicamento");
                        Nm= obj.getString("nombreMedicamento");
                        Ub= obj.getString("cantidadTotalMedicamento");



                        elemento[i] = "\n Id=" + idM +"\n Codigo=" + Cdm + "\n Nombre=" + Nm + "\n Ubicacion=" + Ub;
                    }

                    mostrar();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Error",
                            Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String downloadUrl(String myurl) throws IOException {
        Log.i("URL",""+myurl);
        myurl = myurl.replace(" ","%20");
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("respuesta", "The response is: " + response);
            is = conn.getInputStream();
            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch(IOException e){

            return e.getMessage();
        }finally {
            if (is != null) {
                is.close();
            }
        }
    }
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }


    }
