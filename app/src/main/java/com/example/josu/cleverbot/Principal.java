package com.example.josu.cleverbot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.josu.cleverbot.lib.ChatterBot;
import com.example.josu.cleverbot.lib.ChatterBotFactory;
import com.example.josu.cleverbot.lib.ChatterBotSession;
import com.example.josu.cleverbot.lib.ChatterBotType;

import java.util.ArrayList;
import java.util.Locale;


public class Principal extends ActionBarActivity implements TextToSpeech.OnInitListener{

    private EditText et;
    private EditText tv;
    private TextToSpeech tts;
    private float tono=1;
    private float vel=1;
    private boolean sipuedo;
    private static int CTE=0;
    private static int HABLA=1;
    private ImageButton ib;
    private String respuesta;
    private String idioma;
    private boolean spanish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        et = (EditText)findViewById(R.id.et);
        et.setText("");
        tv = (EditText)findViewById(R.id.tv);
        ib = (ImageButton)findViewById(R.id.ibHablar);
        idioma = "es-ES";
        spanish = true;
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, CTE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        ActionMenuItemView menuEsp = (ActionMenuItemView)findViewById(R.id.action_esp);
        ActionMenuItemView menuIng = (ActionMenuItemView)findViewById(R.id.action_ing);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_esp) {
            tts.setLanguage(new Locale("es","ES"));
            idioma = "es-ES";
            menuIng.setIcon(getApplicationContext().getResources().getDrawable(R.drawable.uk_bw));
            menuEsp.setIcon(getApplicationContext().getResources().getDrawable(R.drawable.sp));
            return true;
        }else if(id == R.id.action_ing){
            tts.setLanguage(new Locale("en","GB"));
            idioma = "en-GB";
            menuEsp.setIcon(getApplicationContext().getResources().getDrawable(R.drawable.sp_bw));
            menuIng.setIcon(getApplicationContext().getResources().getDrawable(R.drawable.uk));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS){
            tts.setLanguage(new Locale("es","ES"));
            tts.setPitch(tono);
            tts.setSpeechRate(vel);
            sipuedo=true;
        } else {
            sipuedo=false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CTE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        }
        if (requestCode == HABLA) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> textos = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                et.setText(textos.get(0));
            }
        }
    }

    class HiloBot extends AsyncTask<String, Void, String> {

        private EditText tv = (EditText)findViewById(R.id.tv);

        public String prueba(String s) throws Exception {
            ChatterBotFactory factory = new ChatterBotFactory();
            ChatterBot bot1= factory.create(ChatterBotType.CLEVERBOT);
            ChatterBotSession bot1session=bot1.createSession();
            return bot1session.think(s);
        }

        @Override
        protected String doInBackground(String... params) {//Otra hebra
            String s = params[0];
            String respuesta = "";
            try {
                respuesta = prueba(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return respuesta;
        }

        @Override
        protected void onPostExecute(String resp) {//Hebra UI
            super.onPostExecute(resp);
            resp = convertFromUTF8(resp);
            tv.append(cambiarColor("\nRobot", Color.GREEN));
            tv.append(cambiarColor("\n" + resp + "\n", Color.BLACK));
            tv.setGravity(Gravity.BOTTOM);
            respuesta = resp;
            ib.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_volume_on));
            ib.setTag("volume");
        }
    }

    public void enviar(View v){
        String aux = et.getText().toString().trim();
        tv.append(cambiarColor("\nJosué", Color.RED));
        tv.append(cambiarColor("\n" + aux + "\n", Color.BLACK));
        tv.setGravity(Gravity.BOTTOM);
        et.setText("");
        HiloBot hf = new HiloBot();
        hf.execute(aux);
    }

    public void hablarReproducir(View v){
        if(ib.getTag().equals("volume"))
            reproducir(respuesta);
        else
            hablar();
    }

    public void hablar(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, idioma);
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Habla ahora");
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,3000);
        startActivityForResult(i,HABLA);
    }

    public void reproducir(String texto){
        if(sipuedo){
            tts.speak(texto, TextToSpeech.QUEUE_ADD, null);
            ib.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_mic));
            ib.setTag("mic");
        }else{
            Toast.makeText(getApplicationContext(), "no se puede reproducir", Toast.LENGTH_SHORT).show();
        }
    }

    public SpannableStringBuilder cambiarColor(String cadena, int color){
        SpannableStringBuilder sb = new SpannableStringBuilder(cadena);
        ForegroundColorSpan fcs = new ForegroundColorSpan(color);
        sb.setSpan(fcs, 0, cadena.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return sb;
    }

    public static String convertFromUTF8(String s) {
        String out = null;
        try {
            out = new String(s.getBytes(), "ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return out;
    }

    // convert from internal Java String format -> UTF-8
    public static String convertToUTF8(String s) {
        String out = null;
        try {
            out = new String(s.getBytes("UTF-8"), "ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
        return out;
    }
}
