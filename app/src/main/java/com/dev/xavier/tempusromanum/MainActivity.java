package com.dev.xavier.tempusromanum;

import android.appwidget.AppWidgetManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Xavier Freyburger
 */
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, TextWatcher {

    private TextView outputDate;
    private EditText dayEditText;
    private EditText monthEditText;
    private EditText yearEditText;
    private RadioGroup eraRadioGroup;
    private boolean lockTextWatcher = false;

    private Date date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        outputDate = findViewById(R.id.outputDate);
        dayEditText = findViewById(R.id.dayEditText);
        monthEditText = findViewById(R.id.monthEditText);
        yearEditText = findViewById(R.id.yearEditText);
        eraRadioGroup = findViewById(R.id.eraRadioGroup);

        if(savedInstanceState != null) {
            restoreSavedInstaceState(savedInstanceState);
        }

        // sélection par défaut de êre moderne
        if(eraRadioGroup.getCheckedRadioButtonId() == -1)
        {
            eraRadioGroup.check(R.id.adRadioButton);
        }

        updateDate();

        dayEditText.addTextChangedListener(this);
        monthEditText.addTextChangedListener(this);
        yearEditText.addTextChangedListener(this);
        eraRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateDate();
            }
        });


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("simple text",outputDate.getText());
                clipboard.setPrimaryClip(clip);
                Snackbar.make(view, getString(R.string.text_copied_to_clipboard), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        FloatingActionButton fab2 = findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Force today date
                updateDate(true);
                Snackbar.make(view, "The date has been reset", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setupSharedPreferences();
    }

    private void updateDate()
    {
        updateDate(false);
    }

    private void updateDate(boolean forceNewDate)
    {
        Integer d = null;
        Integer m = null;
        Integer y = null;

        boolean updateDay = false;
        boolean updateMonth = false;
        boolean updateYear = false;

        if(!forceNewDate) {
            d = dayEditText.getText() == null || dayEditText.getText().length() == 0 ? null : Integer.valueOf(dayEditText.getText().toString());
            m = monthEditText.getText() == null || monthEditText.getText().length() == 0 ? null : Integer.valueOf(monthEditText.getText().toString());
            y = yearEditText.getText() == null || yearEditText.getText().length() == 0 ? null : Integer.valueOf(yearEditText.getText().toString());


            // Contrôle de la validité du jour saisi
            if (d != null) {
                int newd = d;
                if (d <= 0) {
                    d = null;
                } else {
                    newd = d % 32;
                    if (newd == 0) {
                        newd = 1;
                    }
                }
                if(d!= null)
                {
                    updateDay = newd != d;
                    d = newd;
                }
            }

            // Contôle du mois saisi
            if (m != null) {
                int newm = m;
                if (m <= 0) {
                    m = null;
                } else {
                    newm = m % 13;
                    if (newm == 0) {
                        newm = 1;
                    }
                }
                if(m != null) {
                    updateMonth = newm != m;
                    m = newm;
                }
            }

            // Contrôle de l'année saisie
            if (y != null) {
                int newy = y;
                if (y <= 0) {
                    y = null;
                } else {
                    newy = y % 10000;
                    if (newy == 0) {
                        newy = 1;
                    }
                }
                if(y != null) {
                    updateYear = newy != y;
                    y = newy;
                }
            }
        }

        if(forceNewDate || (d == null && m == null && y == null))
        {
            // Toutes les zones sont vides, on initialise avec la date du jour
            date = new Date();
        }
        else if(d == null || m == null || y == null)
        {
            // toutes les zones ne sont pas saisie, on laisse tel-quel
            return;
        }
        else
        {
            // Calcul de la date
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, d);
            c.set(Calendar.MONTH, m-1);
            c.set(Calendar.YEAR, y);
            switch (eraRadioGroup.getCheckedRadioButtonId()) {
                case R.id.adRadioButton:
                    c.set(Calendar.ERA, GregorianCalendar.AD);
                    break;
                case R.id.bcRadioButton:
                    c.set(Calendar.ERA, GregorianCalendar.BC);
                    break;
            }
            date = c.getTime();
        }
        //  + " --- " + date.toGMTString()
        outputDate.setText(Calendarium.tempus(date));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        lockTextWatcher = true;
        if(d == null || updateDay || d != calendar.get(Calendar.DAY_OF_MONTH)) {
            dayEditText.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        }
        if(m == null || updateMonth || m != (calendar.get(Calendar.MONTH)+1)) {
            monthEditText.setText(String.valueOf(calendar.get(Calendar.MONTH) + 1));
        }
        if(y == null || updateYear || y != calendar.get(Calendar.YEAR)) {
            yearEditText.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        }
        lockTextWatcher = false;
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void updateWidget() {
        Intent intent = new Intent(this, TempusRomanumWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), TempusRomanumWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Open settings activity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("font_size")) {
            // Update widget
            updateWidget();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("outputDate", outputDate.getText().toString());
        outState.putString("dayEditText", dayEditText.getText().toString());
        outState.putString("monthEditText", monthEditText.getText().toString());
        outState.putString("yearEditText", yearEditText.getText().toString());
        outState.putInt("eraRadioGroup", eraRadioGroup.getCheckedRadioButtonId());
    }

    private void restoreSavedInstaceState(@NonNull Bundle outState)
    {
        outputDate.setText(outState.getString("outputDate"));
        dayEditText.setText(outState.getString("dayEditText"));
        monthEditText.setText(outState.getString("monthEditText"));
        yearEditText.setText(outState.getString("yearEditText"));
        eraRadioGroup.check(outState.getInt("eraRadioGroup"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(!lockTextWatcher) {
            updateDate();
        }
    }

    /*@Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // create intent to update all instances of
        Log.d("MainActivity", "throw update to widget");
        updateWidget();
    }*/
}