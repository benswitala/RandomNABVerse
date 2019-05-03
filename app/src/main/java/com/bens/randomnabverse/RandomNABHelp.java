package com.bens.randomnabverse;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class RandomNABHelp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_nabhelp);
        TextView textView = (TextView)findViewById(R.id.text_view);
    }//onCreate
}//RandomNAHelp
