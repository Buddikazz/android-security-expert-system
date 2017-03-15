package com.example.adityaaadvani.secexpsys;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Security Evaluation Expert System Android Smartphone application.
 * @author Aditya Ashok Advani
 * @version 26 June, 2016
 */
public class MainActivity extends AppCompatActivity {

    //Security Scores
    int OverallSecurityScore = 0;
    int SystemSecurityScore = 0;
    int TotalSystemScore = 0;
    int ApplicationSecurityScore = 0;
    int TotalApplicationScore = 0;
    int ScaledTotalSystemScore = 0;
    int ScaledTotalApplicationScore = 0;

    //Knowledge base
    HashMap<String,Integer>SystemKB = new HashMap<>();
    HashMap<String,data>ApplicationKB = new HashMap<>();

    //get apps on phone
    static List <ApplicationInfo> packages;
    static List <ApplicationInfo> PlayStorepackages;
    static List <ApplicationInfo> ThirdPartypackages;

    //app categories after analysis
    static HashMap<String, String> AppRisk= new HashMap<>();

    //system check
    static boolean isRooted = false;
    static boolean isEncrypted = false;
    static boolean isUSBDebugging = false;
    static boolean isLockEnabled = false;
    static int APILevel = 0;

    //play store query variables
    static String urlstart = "https://42matters.com/api/1/apps/lookup.json?p=";
    static String urltoken = "&access_token=";
    static String tokenID = "f542fd2073d97021db1e7a117110d629f8abb9f1";
    static String packagename;

    //thread variables
    static boolean part1execd = false;
    static boolean part2execd = false;
    static boolean EvalSuccessful = false;
    static boolean Evalinprogress = false;

    //UI Elements
    Button EvalButton;
    Button ReportButton;
    Button Instructions;

    //Reports
    static String FinalReport = "";
    StringBuilder SystemReport = new StringBuilder();
    StringBuilder AppReport = new StringBuilder();

    //method executed when the application is started
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SystemReport.append("\nSYSTEM REPORT\n");
        AppReport.append("\n\nAPP REPORT\n");

        final TextView tresult=(TextView)findViewById(R.id.textView10);
        EvalButton = (Button) findViewById(R.id.button3);
        EvalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(!Evalinprogress){
                    Evalinprogress=true;
                    EvalSuccessful = false;
                    tresult.setText("Evaluation in Progress...");
                Thread ExecSystemCheck = new Thread(new SystemCheck());
                ExecSystemCheck.start();

                Thread ExecPart2 = new Thread(new Exec2());
                ExecPart2.start();

                Thread ExpSysEval = new Thread(new ExecEvaluation());
                ExpSysEval.start();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        ReportButton = (Button) findViewById(R.id.button);
        ReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent myIntent = new Intent(MainActivity.this, Report.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        Instructions = (Button) findViewById(R.id.button2);
        Instructions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent myIntent = new Intent(MainActivity.this, Instructions.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

    }

    //updates the UI elements once the evaluation is completed.
    private class UpdateDisplay extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... params) {

            return "";
        }

        protected void onProgressUpdate(String... params) {

        }

        protected void onPostExecute(String result) {
            TextView toverall=(TextView)findViewById(R.id.textView6);
            toverall.setText(""+OverallSecurityScore+"/100");

            TextView tsystem=(TextView)findViewById(R.id.textView7);
            tsystem.setText(""+ScaledTotalSystemScore+"/50");

            TextView tapp=(TextView)findViewById(R.id.textView8);
            tapp.setText(""+ScaledTotalApplicationScore+"/50");

            TextView tresult=(TextView)findViewById(R.id.textView10);

            if(OverallSecurityScore > 70) {
                tresult.setText("Your device is at HIGH risk");
            } else if(OverallSecurityScore > 50) {
                tresult.setText("Your device is at MODERATE risk");
            } else {
                tresult.setText("Your device is at LOW risk");
            }
        }


    }

    //starts the thread to perform application evaluation
    public class Exec2 implements Runnable{
        @Override
        public void run() {
            while(!part1execd){
                System.out.print("");
            }
            Thread ExecAppCheck = new Thread(new ApplicationCheck());
            ExecAppCheck.start();
        }
    }

    //Rule Engine
    public class ExecEvaluation implements Runnable{
        @Override
        public void run() {
            while(!part2execd){
                System.out.print("");
            }

            //System Security Score Eval
            if(SystemKB.get("Rooted") == 1){
                SystemSecurityScore += 10;
                SystemReport.append("Device is Rooted:\nRisk is HIGH\n\n");
            } else {
                SystemSecurityScore += 4;
            }

            if(SystemKB.get("Encrypted") == 1){
                SystemSecurityScore += 4;
            } else {
                SystemSecurityScore += 10;
                SystemReport.append("Device is not Encrypted:\nRisk is HIGH\n\n");
            }

            if(SystemKB.get("USB Debug") == 1){
                SystemSecurityScore += 10;
                SystemReport.append("USB Debugging is activated on device:\nRisk is HIGH\n\n");
            } else {
                SystemSecurityScore += 4;
            }

            if(SystemKB.get("Locked") == 1){
                SystemSecurityScore += 4;
            } else {
                SystemSecurityScore += 10;
                SystemReport.append("Device lock is not activated:\nRisk is HIGH\n\n");
            }

            SystemSecurityScore += (23-APILevel);
            if(23-APILevel > 2){
                SystemReport.append("Device is running older OS:\nRisk is HIGH\n\n");
            }
            TotalSystemScore = 47;
            System.out.println("\n\n");
            System.out.println("SystemSecurityScore: " + SystemSecurityScore+"/"+TotalSystemScore);


            //Application Security Score Eval
            for(ApplicationInfo pi:ThirdPartypackages){
                if(pi.processName.equals("com.example.adityaaadvani.secexpsys")) continue;
                ApplicationSecurityScore += 25;
                TotalApplicationScore += 25;
                System.out.println(pi.processName + ": 25");
                AppReport.append("Third Party App: "+pi.processName + ":\n Risk is HIGH\n\n");
            }

            int currentappscore;
            for(String pname:ApplicationKB.keySet()){
                currentappscore = 0;
                data d = ApplicationKB.get(pname);
                TotalApplicationScore += 25;

                //Downloads
                if(d.getDownloads_min() > 1000000000){
                    ApplicationSecurityScore += 0;
                    currentappscore += 0;
                } else if(d.getDownloads_min() > 100000000){
                    ApplicationSecurityScore += 1;
                    currentappscore += 1;
                } else if(d.getDownloads_min() > 10000000){
                    ApplicationSecurityScore += 2;
                    currentappscore += 2;
                } else if(d.getDownloads_min() > 1000000){
                    ApplicationSecurityScore += 3;
                    currentappscore += 3;
                } else if(d.getDownloads_min() > 100000){
                    ApplicationSecurityScore +=4;
                    currentappscore += 4;
                } else {
                    ApplicationSecurityScore += 5;
                    currentappscore += 5;
                }

                //Developer Reliability
                if(d.getBadges() != null && d.getBadges().size() > 1){
                    ApplicationSecurityScore += 1;
                    currentappscore += 1;
                } else if(d.getBadges() != null && d.getBadges().size() == 1) {
                    ApplicationSecurityScore += 3;
                    currentappscore += 3;
                } else {
                    ApplicationSecurityScore += 5;
                    currentappscore += 5;
                }

                //Rating
                if(d.getRating() > 4.5){
                    ApplicationSecurityScore += 1;
                    currentappscore += 1;
                } else if(d.getRating() > 3.5){
                    ApplicationSecurityScore += 2;
                    currentappscore +=2;
                } else if(d.getRating() > 3){
                    ApplicationSecurityScore += 3;
                    currentappscore += 3;
                } else if(d.getRating() > 2.5){
                    ApplicationSecurityScore += 4;
                    currentappscore += 4;
                } else {
                    ApplicationSecurityScore += 5;
                    currentappscore += 5;
                }

                //Number of Ratings
                if(d.getNumber_ratings() > 10000000){
                    ApplicationSecurityScore += 1;
                    currentappscore += 1;
                } else if(d.getNumber_ratings() > 1000000){
                    ApplicationSecurityScore += 2;
                    currentappscore += 2;
                } else if(d.getNumber_ratings() > 100000){
                    ApplicationSecurityScore += 3;
                    currentappscore += 3;
                } else if(d.getNumber_ratings() > 10000){
                    ApplicationSecurityScore += 4;
                    currentappscore += 4;
                } else {
                    ApplicationSecurityScore += 5;
                    currentappscore += 5;
                }

                String dates = d.getMarket_update().split("T")[0];
                String[] date = dates.split("-");
                int year = Integer.parseInt(date[0]) - 2000;
                int month = Integer.parseInt(date[1]) + (year*12);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String sysDate = sdf.format(new Date());
                date = sysDate.split("-");
                int sysYear = Integer.parseInt(date[0]) - 2000;
                int sysMonth = Integer.parseInt(date[1]) + (sysYear*12);

                if(sysMonth - month > 9){
                    ApplicationSecurityScore += 5;
                    currentappscore += 5;
                } else if (sysMonth - month > 7){
                    ApplicationSecurityScore += 4;
                    currentappscore += 4;
                } else if (sysMonth - month > 5){
                    ApplicationSecurityScore += 3;
                    currentappscore += 3;
                } else if (sysMonth - month > 3){
                    ApplicationSecurityScore += 2;
                    currentappscore += 2;
                } else {
                    ApplicationSecurityScore += 1;
                    currentappscore += 1;
                }

                System.out.println(d.getTitle() + ": " + currentappscore);
                if(currentappscore > 16) {
                    AppRisk.put(d.getTitle(), "HIGH");
                    AppReport.append(d.getTitle() + ":\n Risk is HIGH\n\n");
                } else if (currentappscore > 11){
                    AppRisk.put(d.getTitle(), "MODERATE");
                    AppReport.append(d.getTitle() + ":\n Risk is MODERATE\n\n");
                } else {
                    AppRisk.put(d.getTitle(), "LOW");
                    AppReport.append(d.getTitle() + ":\n Risk is LOW\n\n");
                }

            }

        //Combining the Scores into the overall security score
            System.out.println("\n\n");
            //%score of System, scaled down to overall's 50%
            ScaledTotalSystemScore = (SystemSecurityScore * 50)/TotalSystemScore;
            System.out.println("Total System Security Score: " + SystemSecurityScore + "/" + TotalSystemScore);
            System.out.println("Scaled Total System Security Score: " + ScaledTotalSystemScore);

            //%score of Applications, scaled down to overall's 50%
            ScaledTotalApplicationScore = (ApplicationSecurityScore * 50)/TotalApplicationScore;
            System.out.println("Total Application Security Score: " + ApplicationSecurityScore + "/" + TotalApplicationScore);
            System.out.println("Scaled Total Application Security Score: " + ScaledTotalApplicationScore);

            //Overall Score = (int) additionOfAboveTwo
            OverallSecurityScore = ScaledTotalSystemScore + ScaledTotalApplicationScore;
            System.out.println("Overall Score: " + OverallSecurityScore);
            //low overall -> more secure

            System.out.println("\n\nREPORT");
            for(String name : AppRisk.keySet()){
                System.out.println(name + ": " + AppRisk.get(name));
            }

            new UpdateDisplay().execute("","","");
            FinalReport = SystemReport.toString() + AppReport.toString();
            EvalSuccessful = true;
        }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //thread of this class is executed to acquire the application data using the API
    public class ApplicationCheck implements Runnable{
        @Override
        public void run() {
            appchecker ac = new appchecker();
            for (ApplicationInfo pi : packages) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                packagename = pi.processName;

                String url = urlstart + packagename + urltoken + tokenID;

                String resp = ac.getJSonData(url);
                if(resp != null) {

                    data msg = new Gson().fromJson(resp, data.class);

                    ApplicationKB.put(packagename, msg);
                    System.out.println(msg.getPackage_name());
                    System.out.println(msg.getDownloads_min());
                    System.out.println(msg.getRating());
                    System.out.println(msg.getTitle());
                    System.out.println(msg.getMarket_update());
                    System.out.println(msg.getDeveloper());
                    System.out.println(msg.getCat_int());
                    System.out.println(msg.getNumber_ratings());
                    System.out.println(msg.getBadges());
                } else {
                    ThirdPartypackages.add(pi);
                }
            }
            part2execd = true;
        }
    }

    //class to store the metric data for every application
    public class data{
        String package_name;
        long downloads_min;
        double rating;
        String title;
        String market_update;
        String developer;
        int cat_int;
        int number_ratings;
        List<String> badges;

        public void setPackage_name(String package_name){
            this.package_name = package_name;
        }

        public void setDownloads_min(long downloads_min){
            this.downloads_min = downloads_min;
        }

        public void setRating(double rating){
            this.rating = rating;
        }

        public void setTitle(String title){
            this.title = title;
        }

        public void setMarket_update(String market_update){
            this.market_update = market_update;
        }

        public void setDeveloper(String developer){
            this.developer = developer;
        }

        public void setCat_int(int cat_int){
            this.cat_int = cat_int;
        }

        public void setNumber_ratings(int number_ratings){
            this.number_ratings = number_ratings;
        }

        public void setBadge(List<String> badges){
            this.badges = badges;
        }

        public String getPackage_name(){
            return package_name;
        }

        public long getDownloads_min(){
            return downloads_min;
        }

        public double getRating(){
            return rating;
        }

        public String getTitle(){
            return title;
        }

        public String getMarket_update(){
            return market_update;
        }

        public String getDeveloper(){
            return developer;
        }

        public int getCat_int(){
            return cat_int;
        }

        public int getNumber_ratings(){
            return number_ratings;
        }

        public List<String> getBadges(){
            return badges;
        }

    }

    //this class handles getting the response data from the API
    public class appchecker{
        public String getJSonData(String url){
            int timeout = 999999;
            HttpURLConnection conn = null;
            try {
                URL u = new URL(url);
                conn = (HttpURLConnection) u.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-length", "0");
                conn.setUseCaches(false);
                conn.setAllowUserInteraction(false);
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                conn.connect();


                int status = conn.getResponseCode();

                System.out.println("status: " + status);

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                return sb.toString();

            } catch (MalformedURLException ex) {
            } catch (IOException ex) {
            } finally {
                if (conn != null) {
                    try {
                        conn.disconnect();
                    } catch (Exception ex) {
                    }
                }
            }
           return null;
        }
    }


    //This class is used for creating the knowledge base for the System and User Settings
    public class SystemCheck implements Runnable{
        @Override
        public void run() {
            Syschecker sc = new Syschecker();

            isRooted = sc.checkrooted();
            System.out.println("Rooted: " + isRooted);
            if(isRooted){
                SystemKB.put("Rooted",1);
            } else {
                SystemKB.put("Rooted",0);
            }

            isEncrypted = sc.checkencrypted();
            System.out.println("Encrypted: " + isEncrypted);
            if(isEncrypted){
                SystemKB.put("Encrypted",1);
            } else {
                SystemKB.put("Encrypted",0);
            }

            isUSBDebugging = sc.checkusbdebug();
            System.out.println("USB Debug: " + isUSBDebugging);
            if(isUSBDebugging){
                SystemKB.put("USB Debug",1);
            } else {
                SystemKB.put("USB Debug",0);
            }

            isLockEnabled = sc.checklock();
            System.out.println("Locked: " + isLockEnabled);
            if(isLockEnabled){
                SystemKB.put("Locked",1);
            } else {
                SystemKB.put("Locked",0);
            }

            APILevel = sc.getAPILevel();
            System.out.println("API Level: " + APILevel);
            SystemKB.put("API Level", APILevel);

            sc.getapplist();

            part1execd = true;
        }
    }


    //this class is used for acquiring the data from the System and User Settings
    public class Syschecker{

        public boolean checkrooted(){
            boolean rooted = false;

            //check tags
            String tags = android.os.Build.TAGS;
            if (tags != null && tags.contains("test-keys")) {
                rooted = true;
                System.out.println("Root true at block 1");
                return rooted;
            }

            //check existing root directories
            try {
                File file = new File("/system/app/Superuser.apk");
                if (file.exists()) {
                    rooted = true;
                    System.out.println("Root true at block 2");
                    return rooted;
                }
                file = new File("/sbin/su");
                if (file.exists()) {
                    rooted = true;
                    System.out.println("Root true at block 3");
                    return rooted;
                }
                file = new File("/system/bin/su");
                if (file.exists()) {
                    rooted = true;
                    System.out.println("Root true at block 4");
                    return rooted;
                }
                file = new File("/system/xbin/su");
                if (file.exists()) {
                    rooted = true;
                    System.out.println("Root true at block 5");
                    return rooted;
                }
                file = new File("/data/local/xbin/su");
                if (file.exists()) {
                    rooted = true;
                    System.out.println("Root true at block 6");
                    return rooted;
                }
                file = new File("/data/local/bin/su");
                if (file.exists()) {
                    rooted = true;
                    System.out.println("Root true at block 7");
                    return rooted;
                }
                file = new File("/system/sd/xbin/su");
                if (file.exists()) {
                    rooted = true;
                    System.out.println("Root true at block 8");
                    return rooted;
                }
                file = new File("/system/bin/failsafe/su");
                if (file.exists()) {
                    rooted = true;
                    System.out.println("Root true at block 9");
                    return rooted;
                }
                file = new File("/data/local/su");
                if (file.exists()) {
                    rooted = true;
                    System.out.println("Root true at block 10");
                    return rooted;
                }
            } catch (Exception e1) {
            }

            return rooted;
        }

        public boolean checkencrypted(){
            boolean encrypted = false;
            DevicePolicyManager dpm;
            dpm = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
            int encryptCode = dpm.getStorageEncryptionStatus();
            if(encryptCode > 2){
                encrypted = true;
            }
            return encrypted;
        }

        public boolean checkusbdebug(){
            boolean usbdebug = false;
            int adb = Settings.Secure.getInt(getContentResolver(), Settings.Global.ADB_ENABLED, 0);
            if(adb == 1){
                usbdebug = true;
            }
            return usbdebug;
        }


        public boolean checklock(){
            boolean islock = false;
            KeyguardManager kgm = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if (kgm.isKeyguardSecure()) {
                islock = true;
            }
            return islock;
        }

        public int getAPILevel(){
            int level;
            level = Build.VERSION.SDK_INT;
            return level;
        }

        public void getapplist(){
            final PackageManager pm = getPackageManager();
            List <ApplicationInfo> packages1 = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            PlayStorepackages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            ThirdPartypackages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            packages.clear();
            PlayStorepackages.clear();
            ThirdPartypackages.clear();

            for (ApplicationInfo packageInfo : packages1) {
                if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    packages.add(packageInfo);
                }
            }

            for (ApplicationInfo pi : packages){
                System.out.println( "Installed package :" + pi.packageName);
            }
        }
    }





}
