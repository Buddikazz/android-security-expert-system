package com.example.adityaaadvani.secexpsys;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Report extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        String rep = MainActivity.FinalReport;
        TextView toverall=(TextView)findViewById(R.id.textView11);
        toverall.setText(rep);
    }
}
