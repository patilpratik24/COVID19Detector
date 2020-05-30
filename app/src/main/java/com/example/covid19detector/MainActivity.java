package com.example.covid19detector;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    Button buttonNext;
    public Connection con;
    public ProgressBar progressBar;
    private Spinner spinner_gender, spinner_dia, spinner_heart;
    String name;
    String age, gen, diabetic, heart, anyOther;
    private TextInputLayout tilName, tilAge, tilanyOther;
    public TextInputEditText editTextName, editTextAnyOther, editTextAge;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tilName = (TextInputLayout) findViewById(R.id.textInputLayout7);
        tilAge = (TextInputLayout) findViewById(R.id.textInputLayout9);
        tilanyOther = (TextInputLayout) findViewById(R.id.textInputLayout10);

        editTextName = (TextInputEditText)findViewById(R.id.editText_name);
        editTextAge =  (TextInputEditText) findViewById(R.id.editText_age);
        editTextAnyOther = (TextInputEditText) findViewById(R.id.editText_anyOther);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        editTextName.addTextChangedListener(nextTextWatcher);
        editTextAge.addTextChangedListener(nextTextWatcher);
        editTextAnyOther.addTextChangedListener(nextTextWatcher);



        spinner_gender = (Spinner)findViewById(R.id.spinner_gender);

        final List<String> gender = new ArrayList<>();
        gender.add(0,"Gender");
        gender.add("Male");
        gender.add("Female");
        gender.add("Other");

        ArrayAdapter<String> dataAdapter;
        dataAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, gender);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_gender.setAdapter(dataAdapter);
        spinner_gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.LTGRAY);
                if(parent.getItemAtPosition(position).equals("Gender")){
                    //Toast.makeText(parent.getContext(),"Please Select Gender", Toast.LENGTH_SHORT).show();

                }
                else{
                    String item = parent.getItemAtPosition(position).toString();
                    //Toast.makeText(parent.getContext(),"Selected: "+item, Toast.LENGTH_SHORT).show();
                    gen = item;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_dia = (Spinner)findViewById(R.id.spinner_dai);

        List<String> dia = new ArrayList<>();
        dia.add("Yes");
        dia.add("No");
        dia.add("Not Sure");

        ArrayAdapter<String> dataAdapterdia;
        dataAdapterdia = new ArrayAdapter(this, android.R.layout.simple_spinner_item, dia);
        dataAdapterdia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_dia.setAdapter(dataAdapterdia);
        spinner_dia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.LTGRAY);

                if(parent.getItemAtPosition(position).equals("Are You Diabetic?")){
                    //Toast.makeText(parent.getContext(),"Please Select Gender", Toast.LENGTH_SHORT).show();

                }
                else{
                    String item = parent.getItemAtPosition(position).toString();
                    //Toast.makeText(parent.getContext(),"Diabetic: "+item, Toast.LENGTH_SHORT).show();
                    diabetic = item;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_heart = (Spinner)findViewById(R.id.spinner_heart);

        final List<String> heartlist = new ArrayList<>();
        heartlist.add("Yes");
        heartlist.add("No");

        ArrayAdapter<String> dataAdapterheart;
        dataAdapterheart = new ArrayAdapter(this, android.R.layout.simple_spinner_item, heartlist);
        dataAdapterheart.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_heart.setAdapter(dataAdapterheart);
        spinner_heart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.LTGRAY);

                if(parent.getItemAtPosition(position).equals("Are You Diabetic?")){
                    //Toast.makeText(parent.getContext(),"Please Select Gender", Toast.LENGTH_SHORT).show();

                }
                else{
                    String item = parent.getItemAtPosition(position).toString();
                    //Toast.makeText(parent.getContext(),"Any Underlying Heart Condition: "+item, Toast.LENGTH_SHORT).show();
                    heart = item;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        buttonNext = (Button)findViewById(R.id.btn_Next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    name = editTextName.getText().toString();
                    age = editTextAge.getText().toString();
                    anyOther = editTextAnyOther.getText().toString();
                    Intent intent = new Intent(MainActivity.this, load.class);
                    intent.putExtra("name", name);
                    intent.putExtra("age", age);
                    intent.putExtra("gen", gen);
                    intent.putExtra("anyOther", anyOther);
                    intent.putExtra("diabetic", diabetic);
                    intent.putExtra("heart", heart);
                    startActivity(intent);



                }
            });
    }


    private TextWatcher nextTextWatcher = new TextWatcher(){

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
                String namein = editTextName.getText().toString().trim();
                String agein = editTextAge.getText().toString().trim();
                String anyOtherin = editTextAnyOther.getText().toString().trim();
                buttonNext.setEnabled(!namein.isEmpty() && !agein.isEmpty() && !anyOtherin.isEmpty());



        }

        @Override
        public void afterTextChanged(Editable s) {

           /* String namein = editTextName.getText().toString().trim();
            String agein = editTextAge.getText().toString().trim();
            String anyOtherin = editTextAnyOther.getText().toString().trim();
            if (namein.isEmpty() || agein.isEmpty() || anyOtherin.isEmpty())
            {
                Toast.makeText(MainActivity.this, "Please enter all details!",Toast.LENGTH_SHORT).show();
            }*/

        }
    };


}
