package com.bens.randomnabverse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private TextView bottomTextView;
    private Button button;
    private long time = 0;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private final long interval = 24 * 60 * 60 * 1000;
    private String verse;
    private Handler handler = new Handler();
    private Runnable r;

    @Override
    public void onRestart(){
        super.onRestart();
    }//onRestart

    @Override
    public void onResume(){
        super.onResume();
    }//onResume

    public void onDestroy(){
        super.onDestroy();

    }//onDestroy

    @Override
    public void onPause(){
        super.onPause();
    }//onDestroy

    @Override
    public void onStop(){
        super.onStop();
    }//onDestroy

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = (TextView) this.findViewById(R.id.text_view);
        bottomTextView = (TextView) this.findViewById(R.id.bottomTextView);
        button = (Button) this.findViewById(R.id.button);

        prefs  = getPreferences(Context.MODE_PRIVATE);
        editor = prefs.edit();

        r = new Runnable() {
            @Override
            public void run() {
                bottomTextView.setText(getString(R.string.nowAvailable));
            }//run
        };

        //if(prefs == null) {
        //    SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        //}//if
        time = prefs.getLong(getString(R.string.time),0);

        long now = System.currentTimeMillis();
        if(time != 0) {
            if(now >= time) {
                long timeToPost = time + interval - now;
                if(timeToPost > 0) {
                    handler.postAtTime(r, timeToPost+SystemClock.uptimeMillis());
                }//if
            }//if
        }//if

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if((currentTime - time) >= interval) {
                    String s = massageVerse(accountForVerseNumberingDiscrepancies(getRandomCatholicBibleVerse()));
                    textView.setText(s);
                    time = currentTime;
                    //SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                    //SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(getString(R.string.time), time);
                    editor.putString(getString(R.string.verse), s);
                    editor.apply();
                    //display message in bottomTextView here -- with Date
                    bottomTextView.setText(getBottomString());
                    handler.postAtTime(r, SystemClock.uptimeMillis()+interval);
                }//if
            }//onClick
        });
    }//onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }//onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openAbout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }//onOptionsItemSelected


    @Override
    protected void onStart(){
        super.onStart();
        if(prefs == null) {
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        }//if
        time = prefs.getLong(getString(R.string.time), 0);
        verse = prefs.getString(getString(R.string.verse), "Tolle et Lege!");
        textView.setText(verse);
        bottomTextView.setText(getBottomString());

    }//onStart

    public void openAbout(){
        Intent intent = new Intent(this, RandomNABHelp.class);
        startActivity(intent);
    }//openHelp

    /**
     * depends on field time
     * @return
     */
    private String getBottomString(){
        String result = null;
        SimpleDateFormat sdf = new SimpleDateFormat("'on' EEE, MMM d, yyyy 'at' hh:mm:ss aaa");
        long currentTime = System.currentTimeMillis();
        if(time == 0){
            result = getString(R.string.only_one_verse_per_day);
        }else if(currentTime > time + interval){
            result = getString(R.string.nowAvailable);
        }else {
            result = "The next random verse will be available "+sdf.format(new Date(time + interval))+".";
        }//if
        return result;
    }//getBottomString

    private String massageVerse(String s){
        String result = s;
        String[] parts = s.split(" ");
        s = parts[0] + " " + parts[1] + ":" + parts[2];
        if(s.charAt(0) == '1' || s.charAt(0) == '2' || s.charAt(0) == '3') {
            result = s.substring(0, 1) + " " + s.substring(1, 2).toUpperCase() + s.substring(2);
        }else if(parts[0].equals("songofsongs")){
            result = "Song of Songs " + parts[1] + ":" + parts[2];
        }else{
            result = s.substring(0,1).toUpperCase() + s.substring(1);
        }//if-else
        return result;

    }//massageVerse

    public String getRandomCatholicBibleVerse() {
        String result;
        Random rnd = new Random();
        int num = rnd.nextInt(35484) + 1;
        result = getCatholicBibleVerse(num);
        return result;

    }//getRandomCatholicBibleVerse

    //returns a string in format "book chapter verse"
    //where verse is what I call the "actual verse number"
    public String getCatholicBibleVerse(int num) {
        String result = null;
        String[] books = getBookNames();
        int[] chapters = getNumberOfChapters();
        int[] verses = getNumberVersesPerChapter();
        int lowerBound = 1;
        int upperBound = 0;
        int chapterIndex = 0;
        for (int i = 0; i < books.length; i++) {         //iterating through the books
            for (int j = 0; j < chapters[i]; j++) {  //iterating through the chapters of this book
                upperBound += verses[chapterIndex];
                if (num >= lowerBound && num <= upperBound) {  //we're in the right chapter
                    int verse = num - lowerBound + 1;
                    result = books[i] + " " + (j + 1) + " " + verse;
                    break;
                }//if
                lowerBound = upperBound + 1;
                chapterIndex++;
            }//for
            if (result != null) {
                break;
            }//if
        }//for
        return result;
    }//getCatholicBibleVerse

    public String[] getBookNames() {
        String[] names = {"genesis", "exodus", "leviticus", "numbers",
                "deuteronomy", "joshua", "judges", "ruth",
                "1samuel", "2samuel", "1kings", "2kings",
                "1chronicles", "2chronicles", "ezra",
                "nehemiah", "tobit", "judith", "esther",
                "1maccabees", "2maccabees", "job", "psalms",
                "proverbs", "ecclesiastes", "songofsongs",
                "wisdom", "sirach", "isaiah", "jeremiah",
                "lamentations", "baruch", "ezekiel", "daniel",
                "hosea", "joel", "amos", "obadiah", "jonah",
                "micah", "nahum", "habakkuk", "zephaniah",
                "haggai", "zechariah", "malachi", "matthew",
                "mark", "luke", "john", "acts", "romans",
                "1corinthians", "2corinthians", "galatians",
                "ephesians", "philippians", "colossians",
                "1thessalonians", "2thessalonians", "1timothy",
                "2timothy", "titus", "philemon", "hebrews",
                "james", "1peter", "2peter", "1john", "2john",
                "3john", "jude", "revelation"};
        return names;

    }//getBookNames

    public int[] getNumberOfChapters() {
        int[] chaptersPerBook = {50, 40, 27, 36, 34, 24, 21, 4, 31, 24,
                22, 25, 29, 36, 10, 13, 14, 16, 10, 16,
                15, 42, 150, 31, 12, 8, 19, 51, 66, 52,
                5, 6, 48, 14, 14, 4, 9, 1, 4, 7, 3, 3,
                3, 2, 14, 3, 28, 16, 24, 21, 28, 16, 16,
                13, 6, 6, 4, 4, 5, 3, 6, 4, 3, 1, 13, 5,
                5, 3, 5, 1, 1, 1, 22};
        return chaptersPerBook;
    }//

    public int[] getNumberVersesPerChapter() {
        int[] array = {31, 25, 24, 26, 32, 22, 24, 22, 29, 32, 32, 20, 18, 24, 21, 16, 27,
                33, 38, 18, 34, 24, 20, 67, 34, 35, 46, 22, 35, 43, 54, 33, 20, 31, 29, 43, 36, 30, 23, 23, 57, 38, 34, 34, 28,
                34, 31, 22, 33, 26, 22, 25, 22, 31, 23, 30, 29, 28, 35, 29, 10, 51, 22, 31, 27, 36, 16, 27, 25, 26, 37, 30, 33,
                18, 40, 37, 21, 43, 46, 38, 18, 35, 23, 35, 35, 38, 29, 31, 43, 38, 17, 16, 17, 35, 26, 23, 38, 36, 24, 20, 47,
                8, 59, 57, 33, 34, 16, 30, 37, 27, 24, 33, 44, 23, 55, 46, 34, 54, 34, 51, 49, 31, 27, 89, 26, 23, 36, 35, 16, 33,
                45, 41, 35, 28, 32, 22, 29, 35, 41, 30, 25, 18, 66, 23, 31, 39, 17, 54, 42, 56, 29, 34, 13, 46, 37, 29, 49, 33,
                25, 26, 20, 29, 22, 32, 31, 19, 29, 23, 22, 20, 22, 21, 20, 23, 29, 26, 22, 19, 19, 26, 69, 28, 20, 30, 52, 29,
                12, 18, 24, 17, 24, 15, 27, 26, 35, 27, 43, 23, 24, 33, 15, 63, 10, 18, 28, 51, 9, 45, 34, 16, 33, 36, 23, 31,
                24, 31, 40, 25, 35, 57, 18, 40, 15, 25, 20, 20, 31, 13, 31, 30, 48, 25, 22, 23, 18, 22, 28, 36, 21, 22, 12, 21,
                17, 22, 27, 27, 15, 25, 23, 52, 35, 23, 58, 30, 24, 42, 16, 23, 28, 23, 44, 25, 12, 25, 11, 31, 13, 27, 32, 39,
                12, 25, 23, 29, 18, 13, 19, 27, 31, 39, 33, 37, 23, 29, 32, 44, 26, 22, 51, 39, 25, 53, 46, 28, 20, 32, 38, 51,
                66, 28, 29, 43, 33, 34, 31, 34, 34, 24, 46, 21, 43, 29, 54, 18, 25, 27, 44, 27, 33, 20, 29, 37, 36, 20, 22, 25,
                29, 38, 20, 41, 37, 37, 21, 26, 20, 37, 20, 30, 0, 55, 24, 43, 41, 66, 40, 40, 44, 14, 47, 41, 14, 17, 29, 43, 27, 17,
                19, 8, 30, 19, 32, 31, 31, 32, 34, 21, 30, 18, 17, 17, 22, 14, 42, 22, 18, 31, 19, 23, 16, 23, 14, 19, 14, 19, 34,
                11, 37, 20, 12, 21, 27, 28, 23, 9, 27, 36, 27, 21, 33, 25, 33, 27, 23, 11, 70, 13, 24, 17, 22, 28, 36, 15, 44, 11,
                20, 38, 17, 19, 19, 72, 18, 37, 40, 36, 47, 31, 22, 14, 17, 21, 23, 17, 17, 21, 6, 13, 18, 22, 18, 15, 16, 28, 10,
                15, 24, 21, 32, 36, 14, 23, 23, 20, 20, 19, 14, 25, 39, 23, 22, 63, 14, 14, 10, 41, 32, 14, 64, 70, 60, 61, 68, 63,
                50, 32, 73, 89, 74, 53, 53, 49, 41, 24, 36, 32, 40, 50, 27, 31, 42, 36, 29, 38, 38, 46, 26, 46, 39, 22, 13, 26, 21,
                27, 30, 21, 22, 36, 21, 20, 25, 28, 22, 35, 22, 16, 21, 29, 29, 34, 30, 17, 25, 6, 14, 23, 28, 25, 31, 40, 22, 33, 37,
                16, 33, 24, 41, 30, 32, 26, 17, 6, 11, 9, 9, 13, 11, 18, 10, 21, 18, 7, 9, 6, 7, 5, 11, 15, 51, 15, 10, 14, 32, 6, 10, 22,
                12, 14, 9, 11, 13, 25, 11, 22, 23, 28, 13, 40, 23, 14, 18, 14, 12, 5, 27, 18, 12, 10, 15, 21, 23, 21, 11, 7, 9, 24, 14, 12,
                12, 18, 14, 9, 13, 12, 11, 14, 20, 8, 36, 37, 6, 24, 20, 28, 23, 11, 13, 21, 72, 13, 20, 17, 8, 19, 13, 14, 17, 7, 19, 53, 17,
                16, 16, 5, 23, 11, 13, 12, 9, 9, 5, 8, 29, 22, 35, 45, 48, 43, 14, 31, 7, 10, 10, 9, 8, 18, 19, 2, 29, 176, 7, 8, 9, 4, 8,
                5, 6, 5, 6, 8, 8, 3, 18, 3, 3, 21, 26, 9, 8, 24, 14, 10, 8, 12, 15, 21, 10, 20, 14, 9, 6, 33, 22, 35, 27, 23, 35, 27, 36, 18,
                32, 31, 28, 25, 35, 33, 33, 28, 24, 29, 30, 31, 29, 35, 34, 28, 28, 27, 28, 27, 33, 31, 18, 26, 22, 17, 19, 12, 29, 17,
                18, 20, 10, 14, 17, 17, 11, 16, 16, 12, 14, 14, 16, 24, 19, 19, 23, 25, 30, 21, 18, 21, 27, 26, 19, 31, 19, 29, 21, 25,
                22, 28, 18, 30, 31, 15, 37, 36, 19, 18, 30, 32, 17, 25, 27, 20, 28, 28, 32, 27, 31, 28, 25, 27, 31, 25, 20, 30, 26, 28,
                25, 31, 24, 34, 31, 26, 29, 30, 34, 35, 30, 26, 25, 33, 23, 26, 20, 27, 25, 16, 29, 30, 31, 22, 26, 6, 30, 13, 25, 23, 20,
                34, 16, 6, 22, 32, 9, 14, 14, 7, 25, 6, 17, 25, 18, 23, 12, 21, 13, 29, 24, 33, 9, 20, 24, 17, 10, 22, 38, 22, 8, 31, 29, 25,
                28, 28, 25, 13, 15, 22, 26, 11, 23, 15, 12, 17, 13, 12, 21, 14, 21, 22, 11, 12, 19, 11, 25, 24, 19, 37, 25, 31, 31, 30, 34,
                23, 25, 25, 23, 17, 27, 22, 21, 21, 27, 23, 15, 18, 14, 30, 40, 10, 38, 24, 22, 17, 32, 24, 40, 44, 26, 22, 19, 32, 21, 28,
                18, 16, 18, 22, 13, 30, 5, 28, 7, 47, 39, 46, 64, 34, 22, 22, 66, 22, 22, 22, 35, 38, 37, 9, 72, 28, 10, 27, 17, 17, 14, 27,
                18, 11, 22, 25, 28, 23, 23, 8, 63, 24, 32, 14, 44, 37, 31, 49, 27, 17, 21, 36, 26, 21, 26, 18, 32, 33, 31, 15, 38, 28, 23,
                29, 49, 26, 20, 27, 31, 25, 24, 23, 35, 21, 49, 100, 34, 30, 29, 28, 27, 27, 21, 45, 13, 64, 42, 9, 25, 5, 19, 15, 11, 16,
                14, 17, 15, 11, 15, 15, 10, 20, 27, 5, 21, 15, 16, 15, 13, 27, 14, 17, 14, 15, 21, 16, 11, 10, 11, 16, 13, 12, 14, 14, 16,
                20, 14, 14, 19, 17, 20, 19, 18, 15, 20, 15, 23, 17, 17, 10, 14, 11, 15, 14, 23, 17, 12, 17, 14, 9, 21, 14, 17, 24, 25, 23,
                17, 25, 48, 34, 29, 34, 38, 42, 30, 50, 58, 36, 39, 28, 27, 35, 30, 34, 46, 46, 39, 51, 46, 75, 66, 20, 45, 28, 35, 41, 43,
                56, 37, 38, 50, 52, 33, 44, 37, 72, 47, 20, 80, 52, 38, 44, 39, 49, 50, 56, 62, 42, 54, 59, 35, 35, 32, 31, 37, 43, 48, 47,
                38, 71, 56, 53, 51, 25, 36, 54, 47, 71, 52, 60, 41, 42, 57, 50, 38, 31, 27, 33, 26, 40, 42, 31, 25, 26, 47, 26, 37, 42, 15,
                60, 40, 43, 49, 30, 25, 52, 28, 41, 40, 34, 28, 40, 38, 40, 30, 35, 27, 27, 32, 44, 31, 32, 29, 31, 25, 21, 23, 25, 39, 33,
                21, 36, 21, 14, 23, 33, 27, 31, 16, 23, 21, 13, 20, 40, 13, 27, 33, 34, 31, 13, 40, 58, 24, 24, 17, 18, 18, 21, 18, 16, 24,
                15, 18, 33, 21, 13, 24, 21, 29, 31, 26, 18, 23, 22, 21, 32, 33, 24, 30, 30, 21, 23, 29, 23, 25, 18, 10, 20, 13, 18, 28, 12,
                17, 18, 20, 15, 16, 16, 25, 21, 18, 26, 17, 22, 16, 15, 15, 25, 14, 18, 19, 16, 14, 20, 28, 13, 28, 39, 40, 29, 25, 27, 26,
                18, 17, 20, 25, 25, 22, 19, 14, 21, 22, 18, 10, 29, 24, 21, 21, 13, 15, 25, 20, 29, 22, 11, 14, 17, 17, 13, 21, 11, 19, 18,
                18, 20, 8, 21, 18, 24, 21, 15, 27, 21};

        return array;
    }//getNumberVersesPerChapter

    public String accountForVerseNumberingDiscrepancies(String verse) {
        String result = verse;
        switch (verse) {
            case "numbers 26 1":
                result = "numbers 26 19";
                break;
            case "numbers 26 2":
                result = "numbers 26 1";
                break;
            case "numbers 26 3":
                result = "numbers 26 2";
                break;
            case "numbers 26 4":
                result = "numbers 26 3";
                break;
            case "numbers 26 5":
                result = "numbers 26 4";
                break;
            case "numbers 26 6":
                result = "numbers 26 5";
                break;
            case "numbers 26 7":
                result = "numbers 26 6";
                break;
            case "numbers 26 8":
                result = "numbers 26 7";
                break;
            case "numbers 26 9":
                result = "numbers 26 8";
                break;
            case "numbers 26 10":
                result = "numbers 26 9";
                break;
            case "numbers 26 11":
                result = "numbers 26 10";
                break;
            case "numbers 26 12":
                result = "numbers 26 11";
                break;
            case "numbers 26 13":
                result = "numbers 26 12";
                break;
            case "numbers 26 14":
                result = "numbers 26 13";
                break;
            case "numbers 26 15":
                result = "numbers 26 14";
                break;
            case "numbers 26 16":
                result = "numbers 26 15";
                break;
            case "numbers 26 17":
                result = "numbers 26 16";
                break;
            case "numbers 26 18":
                result = "numbers 26 17";
                break;
            case "numbers 26 19":
                result = "numbers 26 18";
                break;
            case "numbers 26 20":
                result = "numbers 26 19";
                break;
            case "numbers 26 21":
                result = "numbers 26 20";
                break;
            case "numbers 26 22":
                result = "numbers 26 21";
                break;
            case "numbers 26 23":
                result = "numbers 26 22";
                break;
            case "numbers 26 24":
                result = "numbers 26 23";
                break;
            case "numbers 26 25":
                result = "numbers 26 24";
                break;
            case "numbers 26 26":
                result = "numbers 26 25";
                break;
            case "numbers 26 27":
                result = "numbers 26 26";
                break;
            case "numbers 26 28":
                result = "numbers 26 27";
                break;
            case "numbers 26 29":
                result = "numbers 26 28";
                break;
            case "numbers 26 30":
                result = "numbers 26 29";
                break;
            case "numbers 26 31":
                result = "numbers 26 30";
                break;
            case "numbers 26 32":
                result = "numbers 26 31";
                break;
            case "numbers 26 33":
                result = "numbers 26 32";
                break;
            case "numbers 26 34":
                result = "numbers 26 33";
                break;
            case "numbers 26 35":
                result = "numbers 26 34";
                break;
            case "numbers 26 36":
                result = "numbers 26 35";
                break;
            case "numbers 26 37":
                result = "numbers 26 36";
                break;
            case "numbers 26 38":
                result = "numbers 26 37";
                break;
            case "numbers 26 39":
                result = "numbers 26 38";
                break;
            case "numbers 26 40":
                result = "numbers 26 39";
                break;
            case "numbers 26 41":
                result = "numbers 26 40";
                break;
            case "numbers 26 42":
                result = "numbers 26 41";
                break;
            case "numbers 26 43":
                result = "numbers 26 42";
                break;
            case "numbers 26 44":
                result = "numbers 26 43";
                break;
            case "numbers 26 45":
                result = "numbers 26 44";
                break;
            case "numbers 26 46":
                result = "numbers 26 45";
                break;
            case "numbers 26 47":
                result = "numbers 26 46";
                break;
            case "numbers 26 48":
                result = "numbers 26 47";
                break;
            case "numbers 26 49":
                result = "numbers 26 48";
                break;
            case "numbers 26 50":
                result = "numbers 26 49";
                break;
            case "numbers 26 51":
                result = "numbers 26 50";
                break;
            case "numbers 26 52":
                result = "numbers 26 51";
                break;
            case "numbers 26 53":
                result = "numbers 26 52";
                break;
            case "numbers 26 54":
                result = "numbers 26 53";
                break;
            case "numbers 26 55":
                result = "numbers 26 54";
                break;
            case "numbers 26 56":
                result = "numbers 26 55";
                break;
            case "numbers 26 57":
                result = "numbers 26 56";
                break;
            case "numbers 26 58":
                result = "numbers 26 57";
                break;
            case "numbers 26 59":
                result = "numbers 26 58";
                break;
            case "numbers 26 60":
                result = "numbers 26 59";
                break;
            case "numbers 26 61":
                result = "numbers 26 60";
                break;
            case "numbers 26 62":
                result = "numbers 26 61";
                break;
            case "numbers 26 63":
                result = "numbers 26 62";
                break;
            case "numbers 26 64":
                result = "numbers 26 63";
                break;
            case "numbers 26 65":
                result = "numbers 26 64";
                break;
            case "numbers 26 66":
                result = "numbers 26 65";
                break;
            case "1chronicles 6 46":
                result = "1chronicles 6 49";
                break;
            case "1chronicles 6 47":
                result = "1chronicles 6 50";
                break;
            case "1chronicles 6 48":
                result = "1chronicles 6 46";
                break;
            case "1chronicles 6 49":
                result = "1chronicles 6 47";
                break;
            case "1chronicles 6 50":
                result = "1chronicles 6 48";
                break;
            case "1chronicles 14 1":
                result = "1chronicles 14 12";
                break;
            case "1chronicles 14 2":
                result = "1chronicles 14 13";
                break;
            case "1chronicles 14 3":
                result = "1chronicles 14 14";
                break;
            case "1chronicles 14 4":
                result = "1chronicles 14 15";
                break;
            case "1chronicles 14 5":
                result = "1chronicles 14 16";
                break;
            case "1chronicles 14 6":
                result = "1chronicles 14 17";
                break;
            case "tobit 5 23":
                result = "tobit 5 1";
                break;
            case "tobit 6 1":
                result = "tobit 6 2";
                break;
            case "tobit 6 2":
                result = "tobit 6 3";
                break;
            case "tobit 6 3":
                result = "tobit 6 4";
                break;
            case "tobit 6 4":
                result = "tobit 6 5";
                break;
            case "tobit 6 5":
                result = "tobit 6 6";
                break;
            case "tobit 6 6":
                result = "tobit 6 7";
                break;
            case "tobit 6 7":
                result = "tobit 6 8";
                break;
            case "tobit 6 8":
                result = "tobit 6 9";
                break;
            case "tobit 6 9":
                result = "tobit 6 10";
                break;
            case "tobit 6 10":
                result = "tobit 6 11";
                break;
            case "tobit 6 11":
                result = "tobit 6 12";
                break;
            case "tobit 6 12":
                result = "tobit 6 13";
                break;
            case "tobit 6 13":
                result = "tobit 6 14";
                break;
            case "tobit 6 14":
                result = "tobit 6 15";
                break;
            case "tobit 6 15":
                result = "tobit 6 16";
                break;
            case "tobit 6 16":
                result = "tobit 6 17";
                break;
            case "tobit 6 17":
                result = "tobit 6 18";
                break;
            case "esther 1 18":
                result = "esther 1 1";
                break;
            case "esther 1 19":
                result = "esther 1 2";
                break;
            case "esther 1 20":
                result = "esther 1 3";
                break;
            case "esther 1 21":
                result = "esther 1 4";
                break;
            case "esther 1 22":
                result = "esther 1 5";
                break;
            case "esther 1 23":
                result = "esther 1 6";
                break;
            case "esther 1 24":
                result = "esther 1 7";
                break;
            case "esther 1 25":
                result = "esther 1 8";
                break;
            case "esther 1 26":
                result = "esther 1 9";
                break;
            case "esther 1 27":
                result = "esther 1 10";
                break;
            case "esther 1 28":
                result = "esther 1 11";
                break;
            case "esther 1 29":
                result = "esther 1 12";
                break;
            case "esther 1 30":
                result = "esther 1 13";
                break;
            case "esther 1 31":
                result = "esther 1 14";
                break;
            case "esther 1 32":
                result = "esther 1 15";
                break;
            case "esther 1 33":
                result = "esther 1 16";
                break;
            case "esther 1 34":
                result = "esther 1 17";
                break;
            case "esther 1 35":
                result = "esther 1 18";
                break;
            case "esther 1 36":
                result = "esther 1 19";
                break;
            case "esther 1 37":
                result = "esther 1 20";
                break;
            case "esther 1 38":
                result = "esther 1 21";
                break;
            case "esther 1 39":
                result = "esther 1 22";
                break;
            case "esther 3 14":
                result = "esther 3 1";
                break;
            case "esther 3 15":
                result = "esther 3 2";
                break;
            case "esther 3 16":
                result = "esther 3 3";
                break;
            case "esther 3 17":
                result = "esther 3 4";
                break;
            case "esther 3 18":
                result = "esther 3 5";
                break;
            case "esther 3 19":
                result = "esther 3 6";
                break;
            case "esther 3 20":
                result = "esther 3 7";
                break;
            case "esther 3 21":
                result = "esther 3 14";
                break;
            case "esther 3 22":
                result = "esther 3 15";
                break;
            case "esther 4 18":
                result = "esther 4 1";
                break;
            case "esther 4 19":
                result = "esther 4 2";
                break;
            case "esther 4 20":
                result = "esther 4 3";
                break;
            case "esther 4 21":
                result = "esther 4 4";
                break;
            case "esther 4 22":
                result = "esther 4 5";
                break;
            case "esther 4 23":
                result = "esther 4 6";
                break;
            case "esther 4 24":
                result = "esther 4 7";
                break;
            case "esther 4 25":
                result = "esther 4 8";
                break;
            case "esther 4 26":
                result = "esther 4 9";
                break;
            case "esther 4 27":
                result = "esther 4 10";
                break;
            case "esther 4 28":
                result = "esther 4 11";
                break;
            case "esther 4 29":
                result = "esther 4 12";
                break;
            case "esther 4 30":
                result = "esther 4 13";
                break;
            case "esther 4 31":
                result = "esther 4 14";
                break;
            case "esther 4 32":
                result = "esther 4 15";
                break;
            case "esther 4 33":
                result = "esther 4 16";
                break;
            case "esther 4 34":
                result = "esther 4 17";
                break;
            case "esther 4 35":
                result = "esther 4 18";
                break;
            case "esther 4 36":
                result = "esther 4 19";
                break;
            case "esther 4 37":
                result = "esther 4 20";
                break;
            case "esther 4 38":
                result = "esther 4 21";
                break;
            case "esther 4 39":
                result = "esther 4 22";
                break;
            case "esther 4 40":
                result = "esther 4 23";
                break;
            case "esther 4 41":
                result = "esther 4 24";
                break;
            case "esther 4 42":
                result = "esther 4 25";
                break;
            case "esther 4 43":
                result = "esther 4 26";
                break;
            case "esther 4 44":
                result = "esther 4 27";
                break;
            case "esther 4 45":
                result = "esther 4 28";
                break;
            case "esther 4 46":
                result = "esther 4 29";
                break;
            case "esther 4 47":
                result = "esther 4 30";
                break;
            case "esther 4 48":
                result = "esther 4 1";
                break;
            case "esther 4 49":
                result = "esther 4 2";
                break;
            case "esther 4 50":
                result = "esther 4 3";
                break;
            case "esther 4 51":
                result = "esther 4 4";
                break;
            case "esther 4 52":
                result = "esther 4 5";
                break;
            case "esther 4 53":
                result = "esther 4 6";
                break;
            case "esther 4 54":
                result = "esther 4 7";
                break;
            case "esther 4 55":
                result = "esther 4 8";
                break;
            case "esther 4 56":
                result = "esther 4 9";
                break;
            case "esther 4 57":
                result = "esther 4 10";
                break;
            case "esther 4 58":
                result = "esther 4 11";
                break;
            case "esther 4 59":
                result = "esther 4 12";
                break;
            case "esther 4 60":
                result = "esther 4 13";
                break;
            case "esther 4 61":
                result = "esther 4 14";
                break;
            case "esther 4 62":
                result = "esther 4 15";
                break;
            case "esther 4 63":
                result = "esther 4 16";
                break;
            case "esther 8 13":
                result = "esther 8 1";
                break;
            case "esther 8 14":
                result = "esther 8 2";
                break;
            case "esther 8 15":
                result = "esther 8 3";
                break;
            case "esther 8 16":
                result = "esther 8 4";
                break;
            case "esther 8 17":
                result = "esther 8 5";
                break;
            case "esther 8 18":
                result = "esther 8 6";
                break;
            case "esther 8 19":
                result = "esther 8 7";
                break;
            case "esther 8 20":
                result = "esther 8 8";
                break;
            case "esther 8 21":
                result = "esther 8 9";
                break;
            case "esther 8 22":
                result = "esther 8 10";
                break;
            case "esther 8 23":
                result = "esther 8 11";
                break;
            case "esther 8 24":
                result = "esther 8 12";
                break;
            case "esther 8 25":
                result = "esther 8 13";
                break;
            case "esther 8 26":
                result = "esther 8 14";
                break;
            case "esther 8 27":
                result = "esther 8 15";
                break;
            case "esther 8 28":
                result = "esther 8 16";
                break;
            case "esther 8 29":
                result = "esther 8 17";
                break;
            case "esther 8 30":
                result = "esther 8 18";
                break;
            case "esther 8 31":
                result = "esther 8 19";
                break;
            case "esther 8 32":
                result = "esther 8 20";
                break;
            case "esther 8 33":
                result = "esther 8 21";
                break;
            case "esther 8 34":
                result = "esther 8 22";
                break;
            case "esther 8 35":
                result = "esther 8 23";
                break;
            case "esther 8 36":
                result = "esther 8 24";
                break;
            case "esther 8 37":
                result = "esther 8 13";
                break;
            case "esther 8 38":
                result = "esther 8 14";
                break;
            case "esther 8 39":
                result = "esther 8 15";
                break;
            case "esther 8 40":
                result = "esther 8 16";
                break;
            case "esther 8 41":
                result = "esther 8 17";
                break;
            case "esther 10 4":
                result = "esther 10 1";
                break;
            case "esther 10 5":
                result = "esther 10 2";
                break;
            case "esther 10 6":
                result = "esther 10 3";
                break;
            case "esther 10 7":
                result = "esther 10 4";
                break;
            case "esther 10 8":
                result = "esther 10 5";
                break;
            case "esther 10 9":
                result = "esther 10 6";
                break;
            case "esther 10 10":
                result = "esther 10 7";
                break;
            case "esther 10 11":
                result = "esther 10 8";
                break;
            case "esther 10 12":
                result = "esther 10 9";
                break;
            case "esther 10 13":
                result = "esther 10 10";
                break;
            case "esther 10 14":
                result = "esther 10 11";
                break;
            case "1maccabees 9 34":
                result = "1maccabees 9 [34]";
                break;
            case "job 9 36":
                result = "job 9 10:1";
                break;
            case "job 10 1":
                result = "job 10 2";
                break;
            case "job 10 2":
                result = "job 10 3";
                break;
            case "job 10 3":
                result = "job 10 4";
                break;
            case "job 10 4":
                result = "job 10 5";
                break;
            case "job 10 5":
                result = "job 10 6";
                break;
            case "job 10 6":
                result = "job 10 7";
                break;
            case "job 10 7":
                result = "job 10 8";
                break;
            case "job 10 8":
                result = "job 10 9";
                break;
            case "job 10 9":
                result = "job 10 10";
                break;
            case "job 10 10":
                result = "job 10 11";
                break;
            case "job 10 11":
                result = "job 10 12";
                break;
            case "job 10 12":
                result = "job 10 13";
                break;
            case "job 10 13":
                result = "job 10 14";
                break;
            case "job 10 14":
                result = "job 10 15";
                break;
            case "job 10 15":
                result = "job 10 16";
                break;
            case "job 10 16":
                result = "job 10 17";
                break;
            case "job 10 17":
                result = "job 10 18";
                break;
            case "job 10 18":
                result = "job 10 19";
                break;
            case "job 10 19":
                result = "job 10 20";
                break;
            case "job 10 20":
                result = "job 10 21";
                break;
            case "job 10 21":
                result = "job 10 22";
                break;
            case "wisdom 4 15":
                result = "wisdom 4 16";
                break;
            case "wisdom 4 16":
                result = "wisdom 4 17";
                break;
            case "wisdom 4 17":
                result = "wisdom 4 18";
                break;
            case "wisdom 4 18":
                result = "wisdom 4 19";
                break;
            case "wisdom 4 19":
                result = "wisdom 4 20";
                break;
            case "wisdom 11 27":
                result = "wisdom 11 12:1";
                break;
            case "wisdom 12 1":
                result = "wisdom 12 2";
                break;
            case "wisdom 12 2":
                result = "wisdom 12 3";
                break;
            case "wisdom 12 3":
                result = "wisdom 12 4";
                break;
            case "wisdom 12 4":
                result = "wisdom 12 5";
                break;
            case "wisdom 12 5":
                result = "wisdom 12 6";
                break;
            case "wisdom 12 6":
                result = "wisdom 12 7";
                break;
            case "wisdom 12 7":
                result = "wisdom 12 8";
                break;
            case "wisdom 12 8":
                result = "wisdom 12 9";
                break;
            case "wisdom 12 9":
                result = "wisdom 12 10";
                break;
            case "wisdom 12 10":
                result = "wisdom 12 11";
                break;
            case "wisdom 12 11":
                result = "wisdom 12 12";
                break;
            case "wisdom 12 12":
                result = "wisdom 12 13";
                break;
            case "wisdom 12 13":
                result = "wisdom 12 14";
                break;
            case "wisdom 12 14":
                result = "wisdom 12 15";
                break;
            case "wisdom 12 15":
                result = "wisdom 12 16";
                break;
            case "wisdom 12 16":
                result = "wisdom 12 17";
                break;
            case "wisdom 12 17":
                result = "wisdom 12 18";
                break;
            case "wisdom 12 18":
                result = "wisdom 12 19";
                break;
            case "wisdom 12 19":
                result = "wisdom 12 20";
                break;
            case "wisdom 12 20":
                result = "wisdom 12 21";
                break;
            case "wisdom 12 21":
                result = "wisdom 12 22";
                break;
            case "wisdom 12 22":
                result = "wisdom 12 23";
                break;
            case "wisdom 12 23":
                result = "wisdom 12 24";
                break;
            case "wisdom 12 24":
                result = "wisdom 12 25";
                break;
            case "wisdom 12 25":
                result = "wisdom 12 26";
                break;
            case "wisdom 12 26":
                result = "wisdom 12 27";
                break;
            case "sirach 1 5":
                result = "sirach 1 6";
                break;
            case "sirach 1 6":
                result = "sirach 1 8";
                break;
            case "sirach 1 7":
                result = "sirach 1 9";
                break;
            case "sirach 1 8":
                result = "sirach 1 10";
                break;
            case "sirach 1 9":
                result = "sirach 1 11";
                break;
            case "sirach 1 10":
                result = "sirach 1 12";
                break;
            case "sirach 1 11":
                result = "sirach 1 13";
                break;
            case "sirach 1 12":
                result = "sirach 1 14";
                break;
            case "sirach 1 13":
                result = "sirach 1 15";
                break;
            case "sirach 1 14":
                result = "sirach 1 16";
                break;
            case "sirach 1 15":
                result = "sirach 1 17";
                break;
            case "sirach 1 16":
                result = "sirach 1 18";
                break;
            case "sirach 1 17":
                result = "sirach 1 19";
                break;
            case "sirach 1 18":
                result = "sirach 1 20";
                break;
            case "sirach 1 19":
                result = "sirach 1 21";
                break;
            case "sirach 1 20":
                result = "sirach 1 22";
                break;
            case "sirach 1 21":
                result = "sirach 1 23";
                break;
            case "sirach 1 22":
                result = "sirach 1 24";
                break;
            case "sirach 1 23":
                result = "sirach 1 25";
                break;
            case "sirach 1 24":
                result = "sirach 1 26";
                break;
            case "sirach 1 25":
                result = "sirach 1 27";
                break;
            case "sirach 1 26":
                result = "sirach 1 28";
                break;
            case "sirach 1 27":
                result = "sirach 1 29";
                break;
            case "sirach 1 28":
                result = "sirach 1 30";
                break;
            case "sirach 3 19":
                result = "sirach 3 20";
                break;
            case "sirach 3 20":
                result = "sirach 3 21";
                break;
            case "sirach 3 21":
                result = "sirach 3 22";
                break;
            case "sirach 3 22":
                result = "sirach 3 23";
                break;
            case "sirach 3 23":
                result = "sirach 3 24";
                break;
            case "sirach 3 24":
                result = "sirach 3 25";
                break;
            case "sirach 3 25":
                result = "sirach 3 26";
                break;
            case "sirach 3 26":
                result = "sirach 3 27";
                break;
            case "sirach 3 27":
                result = "sirach 3 28";
                break;
            case "sirach 3 28":
                result = "sirach 3 29";
                break;
            case "sirach 3 29":
                result = "sirach 3 30";
                break;
            case "sirach 3 30":
                result = "sirach 3 31";
                break;
            case "sirach 10 21":
                result = "sirach 10 22";
                break;
            case "sirach 10 22":
                result = "sirach 10 23";
                break;
            case "sirach 10 23":
                result = "sirach 10 24";
                break;
            case "sirach 10 24":
                result = "sirach 10 25";
                break;
            case "sirach 10 25":
                result = "sirach 10 26";
                break;
            case "sirach 10 26":
                result = "sirach 10 27";
                break;
            case "sirach 10 27":
                result = "sirach 10 28";
                break;
            case "sirach 10 28":
                result = "sirach 10 29";
                break;
            case "sirach 10 29":
                result = "sirach 10 30";
                break;
            case "sirach 10 30":
                result = "sirach 10 31";
                break;
            case "sirach 11 15":
                result = "sirach 11 17";
                break;
            case "sirach 11 16":
                result = "sirach 11 18";
                break;
            case "sirach 11 17":
                result = "sirach 11 19";
                break;
            case "sirach 11 18":
                result = "sirach 11 20";
                break;
            case "sirach 11 19":
                result = "sirach 11 21";
                break;
            case "sirach 11 20":
                result = "sirach 11 22";
                break;
            case "sirach 11 21":
                result = "sirach 11 23";
                break;
            case "sirach 11 22":
                result = "sirach 11 24";
                break;
            case "sirach 11 23":
                result = "sirach 11 25";
                break;
            case "sirach 11 24":
                result = "sirach 11 26";
                break;
            case "sirach 11 25":
                result = "sirach 11 27";
                break;
            case "sirach 11 26":
                result = "sirach 11 28";
                break;
            case "sirach 11 27":
                result = "sirach 11 29";
                break;
            case "sirach 11 28":
                result = "sirach 11 30";
                break;
            case "sirach 11 29":
                result = "sirach 11 31";
                break;
            case "sirach 11 30":
                result = "sirach 11 32";
                break;
            case "sirach 11 31":
                result = "sirach 11 33";
                break;
            case "sirach 11 32":
                result = "sirach 11 34";
                break;
            case "sirach 12 7":
                result = "sirach 12 8";
                break;
            case "sirach 12 8":
                result = "sirach 12 9";
                break;
            case "sirach 12 9":
                result = "sirach 12 10";
                break;
            case "sirach 12 10":
                result = "sirach 12 11";
                break;
            case "sirach 12 11":
                result = "sirach 12 12";
                break;
            case "sirach 12 12":
                result = "sirach 12 13";
                break;
            case "sirach 12 13":
                result = "sirach 12 14";
                break;
            case "sirach 12 14":
                result = "sirach 12 15";
                break;
            case "sirach 12 15":
                result = "sirach 12 16";
                break;
            case "sirach 12 16":
                result = "sirach 12 17";
                break;
            case "sirach 12 17":
                result = "sirach 12 18";
                break;
            case "sirach 13 14":
                result = "sirach 13 15";
                break;
            case "sirach 13 15":
                result = "sirach 13 16";
                break;
            case "sirach 13 16":
                result = "sirach 13 17";
                break;
            case "sirach 13 17":
                result = "sirach 13 18";
                break;
            case "sirach 13 18":
                result = "sirach 13 19";
                break;
            case "sirach 13 19":
                result = "sirach 13 20";
                break;
            case "sirach 13 20":
                result = "sirach 13 21";
                break;
            case "sirach 13 21":
                result = "sirach 13 22";
                break;
            case "sirach 13 22":
                result = "sirach 13 23";
                break;
            case "sirach 13 23":
                result = "sirach 13 24";
                break;
            case "sirach 13 24":
                result = "sirach 13 25";
                break;
            case "sirach 13 25":
                result = "sirach 13 26";
                break;
            case "sirach 16 15":
                result = "sirach 16 17";
                break;
            case "sirach 16 16":
                result = "sirach 16 18";
                break;
            case "sirach 16 17":
                result = "sirach 16 19";
                break;
            case "sirach 16 18":
                result = "sirach 16 20";
                break;
            case "sirach 16 19":
                result = "sirach 16 21";
                break;
            case "sirach 16 20":
                result = "sirach 16 22";
                break;
            case "sirach 16 21":
                result = "sirach 16 23";
                break;
            case "sirach 16 22":
                result = "sirach 16 24";
                break;
            case "sirach 16 23":
                result = "sirach 16 25";
                break;
            case "sirach 16 24":
                result = "sirach 16 26";
                break;
            case "sirach 16 25":
                result = "sirach 16 27";
                break;
            case "sirach 16 26":
                result = "sirach 16 28";
                break;
            case "sirach 16 27":
                result = "sirach 16 29";
                break;
            case "sirach 16 28":
                result = "sirach 16 30";
                break;
            case "sirach 17 5":
                result = "sirach 17 6";
                break;
            case "sirach 17 6":
                result = "sirach 17 7";
                break;
            case "sirach 17 7":
                result = "sirach 17 8";
                break;
            case "sirach 17 8":
                result = "sirach 17 9";
                break;
            case "sirach 17 9":
                result = "sirach 17 10";
                break;
            case "sirach 17 10":
                result = "sirach 17 11";
                break;
            case "sirach 17 11":
                result = "sirach 17 12";
                break;
            case "sirach 17 12":
                result = "sirach 17 13";
                break;
            case "sirach 17 13":
                result = "sirach 17 14";
                break;
            case "sirach 17 14":
                result = "sirach 17 15";
                break;
            case "sirach 17 15":
                result = "sirach 17 17";
                break;
            case "sirach 17 16":
                result = "sirach 17 19";
                break;
            case "sirach 17 17":
                result = "sirach 17 20";
                break;
            case "sirach 17 18":
                result = "sirach 17 22";
                break;
            case "sirach 17 19":
                result = "sirach 17 23";
                break;
            case "sirach 17 20":
                result = "sirach 17 24";
                break;
            case "sirach 17 21":
                result = "sirach 17 25";
                break;
            case "sirach 17 22":
                result = "sirach 17 26";
                break;
            case "sirach 17 23":
                result = "sirach 17 27";
                break;
            case "sirach 17 24":
                result = "sirach 17 28";
                break;
            case "sirach 17 25":
                result = "sirach 17 29";
                break;
            case "sirach 17 26":
                result = "sirach 17 30";
                break;
            case "sirach 17 27":
                result = "sirach 17 31";
                break;
            case "sirach 17 28":
                result = "sirach 17 32";
                break;
            case "sirach 18 3":
                result = "sirach 18 4";
                break;
            case "sirach 18 4":
                result = "sirach 18 5";
                break;
            case "sirach 18 5":
                result = "sirach 18 6";
                break;
            case "sirach 18 6":
                result = "sirach 18 7";
                break;
            case "sirach 18 7":
                result = "sirach 18 8";
                break;
            case "sirach 18 8":
                result = "sirach 18 9";
                break;
            case "sirach 18 9":
                result = "sirach 18 10";
                break;
            case "sirach 18 10":
                result = "sirach 18 11";
                break;
            case "sirach 18 11":
                result = "sirach 18 12";
                break;
            case "sirach 18 12":
                result = "sirach 18 13";
                break;
            case "sirach 18 13":
                result = "sirach 18 14";
                break;
            case "sirach 18 14":
                result = "sirach 18 15";
                break;
            case "sirach 18 15":
                result = "sirach 18 16";
                break;
            case "sirach 18 16":
                result = "sirach 18 17";
                break;
            case "sirach 18 17":
                result = "sirach 18 18";
                break;
            case "sirach 18 18":
                result = "sirach 18 19";
                break;
            case "sirach 18 19":
                result = "sirach 18 20";
                break;
            case "sirach 18 20":
                result = "sirach 18 21";
                break;
            case "sirach 18 21":
                result = "sirach 18 22";
                break;
            case "sirach 18 22":
                result = "sirach 18 23";
                break;
            case "sirach 18 23":
                result = "sirach 18 24";
                break;
            case "sirach 18 24":
                result = "sirach 18 25";
                break;
            case "sirach 18 25":
                result = "sirach 18 26";
                break;
            case "sirach 18 26":
                result = "sirach 18 27";
                break;
            case "sirach 18 27":
                result = "sirach 18 28";
                break;
            case "sirach 18 28":
                result = "sirach 18 29";
                break;
            case "sirach 18 29":
                result = "sirach 18 30";
                break;
            case "sirach 18 30":
                result = "sirach 18 31";
                break;
            case "sirach 18 31":
                result = "sirach 18 32";
                break;
            case "sirach 18 32":
                result = "sirach 18 33";
                break;
            case "sirach 19 18":
                result = "sirach 19 20";
                break;
            case "sirach 19 19":
                result = "sirach 19 22";
                break;
            case "sirach 19 20":
                result = "sirach 19 23";
                break;
            case "sirach 19 21":
                result = "sirach 19 24";
                break;
            case "sirach 19 22":
                result = "sirach 19 25";
                break;
            case "sirach 19 23":
                result = "sirach 19 26";
                break;
            case "sirach 19 24":
                result = "sirach 19 27";
                break;
            case "sirach 19 25":
                result = "sirach 19 28";
                break;
            case "sirach 19 26":
                result = "sirach 19 29";
                break;
            case "sirach 19 27":
                result = "sirach 19 30";
                break;
            case "sirach 22 7":
                result = "sirach 22 9";
                break;
            case "sirach 22 8":
                result = "sirach 22 10";
                break;
            case "sirach 22 9":
                result = "sirach 22 11";
                break;
            case "sirach 22 10":
                result = "sirach 22 12";
                break;
            case "sirach 22 11":
                result = "sirach 22 13";
                break;
            case "sirach 22 12":
                result = "sirach 22 14";
                break;
            case "sirach 22 13":
                result = "sirach 22 15";
                break;
            case "sirach 22 14":
                result = "sirach 22 16";
                break;
            case "sirach 22 15":
                result = "sirach 22 17";
                break;
            case "sirach 22 16":
                result = "sirach 22 18";
                break;
            case "sirach 22 17":
                result = "sirach 22 19";
                break;
            case "sirach 22 18":
                result = "sirach 22 20";
                break;
            case "sirach 22 19":
                result = "sirach 22 21";
                break;
            case "sirach 22 20":
                result = "sirach 22 22";
                break;
            case "sirach 22 21":
                result = "sirach 22 23";
                break;
            case "sirach 22 22":
                result = "sirach 22 24";
                break;
            case "sirach 22 23":
                result = "sirach 22 25";
                break;
            case "sirach 22 24":
                result = "sirach 22 26";
                break;
            case "sirach 22 25":
                result = "sirach 22 27";
                break;
            case "sirach 24 18":
                result = "sirach 24 19";
                break;
            case "sirach 24 19":
                result = "sirach 24 20";
                break;
            case "sirach 24 20":
                result = "sirach 24 21";
                break;
            case "sirach 24 21":
                result = "sirach 24 22";
                break;
            case "sirach 24 22":
                result = "sirach 24 23";
                break;
            case "sirach 24 23":
                result = "sirach 24 25";
                break;
            case "sirach 24 24":
                result = "sirach 24 26";
                break;
            case "sirach 24 25":
                result = "sirach 24 27";
                break;
            case "sirach 24 26":
                result = "sirach 24 28";
                break;
            case "sirach 24 27":
                result = "sirach 24 29";
                break;
            case "sirach 24 28":
                result = "sirach 24 30";
                break;
            case "sirach 24 29":
                result = "sirach 24 31";
                break;
            case "sirach 24 30":
                result = "sirach 24 32";
                break;
            case "sirach 24 31":
                result = "sirach 24 33";
                break;
            case "sirach 25 12":
                result = "sirach 25 13";
                break;
            case "sirach 25 13":
                result = "sirach 25 14";
                break;
            case "sirach 25 14":
                result = "sirach 25 15";
                break;
            case "sirach 25 15":
                result = "sirach 25 16";
                break;
            case "sirach 25 16":
                result = "sirach 25 17";
                break;
            case "sirach 25 17":
                result = "sirach 25 18";
                break;
            case "sirach 25 18":
                result = "sirach 25 19";
                break;
            case "sirach 25 19":
                result = "sirach 25 20";
                break;
            case "sirach 25 20":
                result = "sirach 25 21";
                break;
            case "sirach 25 21":
                result = "sirach 25 22";
                break;
            case "sirach 25 22":
                result = "sirach 25 23";
                break;
            case "sirach 25 23":
                result = "sirach 25 24";
                break;
            case "sirach 25 24":
                result = "sirach 25 25";
                break;
            case "sirach 25 25":
                result = "sirach 25 26";
                break;
            case "sirach 26 19":
                result = "sirach 26 28";
                break;
            case "sirach 26 20":
                result = "sirach 26 29";
                break;
            case "sirach 31 14":
                result = "sirach 31 15";
                break;
            case "sirach 31 15":
                result = "sirach 31 14";
                break;
            case "sirach 33 20":
                result = "sirach 33 20a";
                break;
            case "sirach 33 22":
                result = "sirach 33 20b";
                break;
            case "sirach 33 23":
                result = "sirach 33 22";
                break;
            case "sirach 33 24":
                result = "sirach 33 23";
                break;
            case "sirach 33 25":
                result = "sirach 33 24";
                break;
            case "sirach 33 26":
                result = "sirach 33 25";
                break;
            case "sirach 33 27":
                result = "sirach 33 26";
                break;
            case "sirach 33 28":
                result = "sirach 33 27";
                break;
            case "sirach 33 29":
                result = "sirach 33 28";
                break;
            case "sirach 33 30":
                result = "sirach 33 29";
                break;
            case "sirach 33 31":
                result = "sirach 33 30";
                break;
            case "sirach 33 32":
                result = "sirach 33 31";
                break;
            case "sirach 33 33":
                result = "sirach 33 32";
                break;
            case "sirach 33 34":
                result = "sirach 33 33";
                break;
            case "sirach 36 14":
                result = "sirach 36 16";
                break;
            case "sirach 36 15":
                result = "sirach 36 17";
                break;
            case "sirach 36 16":
                result = "sirach 36 18";
                break;
            case "sirach 36 17":
                result = "sirach 36 19";
                break;
            case "sirach 36 18":
                result = "sirach 36 20";
                break;
            case "sirach 36 19":
                result = "sirach 36 21";
                break;
            case "sirach 36 20":
                result = "sirach 36 22";
                break;
            case "sirach 36 21":
                result = "sirach 36 23";
                break;
            case "sirach 36 22":
                result = "sirach 36 24";
                break;
            case "sirach 36 23":
                result = "sirach 36 25";
                break;
            case "sirach 36 24":
                result = "sirach 36 26";
                break;
            case "sirach 36 25":
                result = "sirach 36 27";
                break;
            case "sirach 36 26":
                result = "sirach 36 28";
                break;
            case "sirach 36 27":
                result = "sirach 36 29";
                break;
            case "sirach 36 28":
                result = "sirach 36 30";
                break;
            case "sirach 36 29":
                result = "sirach 36 31";
                break;
            case "sirach 37 21":
                result = "sirach 37 22";
                break;
            case "sirach 37 22":
                result = "sirach 37 23";
                break;
            case "sirach 37 23":
                result = "sirach 37 24";
                break;
            case "sirach 37 24":
                result = "sirach 37 25";
                break;
            case "sirach 37 25":
                result = "sirach 37 26";
                break;
            case "sirach 37 26":
                result = "sirach 37 27";
                break;
            case "sirach 37 27":
                result = "sirach 37 28";
                break;
            case "sirach 37 28":
                result = "sirach 37 29";
                break;
            case "sirach 37 29":
                result = "sirach 37 30";
                break;
            case "sirach 37 30":
                result = "sirach 37 31";
                break;
            case "sirach 41 14":
                result = "sirach 41 14b";
                break;
            case "sirach 41 16":
                result = "sirach 41 14a";
                break;
            case "sirach 41 17":
                result = "sirach 41 16a";
                break;
            case "sirach 41 18":
                result = "sirach 41 16b";
                break;
            case "sirach 41 19":
                result = "sirach 41 17";
                break;
            case "sirach 41 20":
                result = "sirach 41 18";
                break;
            case "sirach 41 21":
                result = "sirach 41 19";
                break;
            case "sirach 41 22":
                result = "sirach 41 21";
                break;
            case "sirach 41 23":
                result = "sirach 41 20a";
                break;
            case "sirach 41 24":
                result = "sirach 41 21c";
                break;
            case "sirach 41 25":
                result = "sirach 41 20b";
                break;
            case "sirach 41 26":
                result = "sirach 41 22";
                break;
            case "sirach 47 11":
                result = "sirach 47 9b";
                break;
            case "sirach 47 12":
                result = "sirach 47 10b";
                break;
            case "sirach 47 13":
                result = "sirach 47 11";
                break;
            case "sirach 47 14":
                result = "sirach 47 12";
                break;
            case "sirach 47 15":
                result = "sirach 47 13";
                break;
            case "sirach 47 16":
                result = "sirach 47 14";
                break;
            case "sirach 47 17":
                result = "sirach 47 15";
                break;
            case "sirach 47 18":
                result = "sirach 47 16";
                break;
            case "sirach 47 19":
                result = "sirach 47 17";
                break;
            case "sirach 47 20":
                result = "sirach 47 18";
                break;
            case "sirach 47 21":
                result = "sirach 47 19";
                break;
            case "sirach 47 22":
                result = "sirach 47 20";
                break;
            case "sirach 47 23":
                result = "sirach 47 21";
                break;
            case "sirach 47 24":
                result = "sirach 47 22";
                break;
            case "sirach 47 25":
                result = "sirach 47 23";
                break;
            case "sirach 47 26":
                result = "sirach 47 24";
                break;
            case "sirach 47 27":
                result = "sirach 47 25";
                break;
            case "amos 5 7":
                result = "amos 5 8";
                break;
            case "amos 5 8":
                result = "amos 5 9";
                break;
            case "amos 5 9":
                result = "amos 5 7";
                break;
            case "matthew 23 14":
                result = "matthew 23 [14]";
                break;
            case "mark 7 16":
                result = "mark 7 [16]";
                break;
            case "mark 9 44":
                result = "mark 9 [44]";
                break;
            case "mark 9 46":
                result = "mark 9 [46]";
                break;
            case "mark 11 26":
                result = "mark 11 [26]";
                break;
            case "mark 15 28":
                result = "mark 15 [28]";
                break;
            case "luke 17 36":
                result = "luke 17 [36]";
                break;
            case "luke 23 17":
                result = "luke 23 [17]";
                break;
            case "john 8 1":
                result = "john 8 7:53";
                break;
            case "john 8 2":
                result = "john 8 1";
                break;
            case "john 8 3":
                result = "john 8 2";
                break;
            case "john 8 4":
                result = "john 8 3";
                break;
            case "john 8 5":
                result = "john 8 4";
                break;
            case "john 8 6":
                result = "john 8 5";
                break;
            case "john 8 7":
                result = "john 8 6";
                break;
            case "john 8 8":
                result = "john 8 7";
                break;
            case "john 8 9":
                result = "john 8 8";
                break;
            case "john 8 10":
                result = "john 8 9";
                break;
            case "john 8 11":
                result = "john 8 10";
                break;
            case "john 8 12":
                result = "john 8 11";
                break;
            case "john 8 13":
                result = "john 8 12";
                break;
            case "john 8 14":
                result = "john 8 13";
                break;
            case "john 8 15":
                result = "john 8 14";
                break;
            case "john 8 16":
                result = "john 8 15";
                break;
            case "john 8 17":
                result = "john 8 16";
                break;
            case "john 8 18":
                result = "john 8 17";
                break;
            case "john 8 19":
                result = "john 8 18";
                break;
            case "john 8 20":
                result = "john 8 19";
                break;
            case "john 8 21":
                result = "john 8 20";
                break;
            case "john 8 22":
                result = "john 8 21";
                break;
            case "john 8 23":
                result = "john 8 22";
                break;
            case "john 8 24":
                result = "john 8 23";
                break;
            case "john 8 25":
                result = "john 8 24";
                break;
            case "john 8 26":
                result = "john 8 25";
                break;
            case "john 8 27":
                result = "john 8 26";
                break;
            case "john 8 28":
                result = "john 8 27";
                break;
            case "john 8 29":
                result = "john 8 28";
                break;
            case "john 8 30":
                result = "john 8 29";
                break;
            case "john 8 31":
                result = "john 8 30";
                break;
            case "john 8 32":
                result = "john 8 31";
                break;
            case "john 8 33":
                result = "john 8 32";
                break;
            case "john 8 34":
                result = "john 8 33";
                break;
            case "john 8 35":
                result = "john 8 34";
                break;
            case "john 8 36":
                result = "john 8 35";
                break;
            case "john 8 37":
                result = "john 8 36";
                break;
            case "john 8 38":
                result = "john 8 37";
                break;
            case "john 8 39":
                result = "john 8 38";
                break;
            case "john 8 40":
                result = "john 8 39";
                break;
            case "john 8 41":
                result = "john 8 40";
                break;
            case "john 8 42":
                result = "john 8 41";
                break;
            case "john 8 43":
                result = "john 8 42";
                break;
            case "john 8 44":
                result = "john 8 43";
                break;
            case "john 8 45":
                result = "john 8 44";
                break;
            case "john 8 46":
                result = "john 8 45";
                break;
            case "john 8 47":
                result = "john 8 46";
                break;
            case "john 8 48":
                result = "john 8 47";
                break;
            case "john 8 49":
                result = "john 8 48";
                break;
            case "john 8 50":
                result = "john 8 49";
                break;
            case "john 8 51":
                result = "john 8 50";
                break;
            case "john 8 52":
                result = "john 8 51";
                break;
            case "john 8 53":
                result = "john 8 52";
                break;
            case "john 8 54":
                result = "john 8 53";
                break;
            case "john 8 55":
                result = "john 8 54";
                break;
            case "john 8 56":
                result = "john 8 55";
                break;
            case "john 8 57":
                result = "john 8 56";
                break;
            case "john 8 58":
                result = "john 8 57";
                break;
            case "john 8 59":
                result = "john 8 58";
                break;
            case "john 8 60":
                result = "john 8 59";
                break;
        }//switch
        return result;
    }//accountForVerseNumberingDiscrepancies

}
