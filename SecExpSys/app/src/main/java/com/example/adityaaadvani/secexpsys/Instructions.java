package com.example.adityaaadvani.secexpsys;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Instructions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        String Instruct = "Instructions:\n This is an Security Evaluation Expert System Application Developed for the Android Smartphones as a course project." +
                "\nThe main goal is to create a Hierarchical Expert System for the Android Smartphone where evaluations of two different domains are combined to create an overall rating." +
                "\n\nThese two domains are:" +
                "\n1. The System and User Settings." +
                "\n2. The Applications Installed." +
                "\n\nThe Expert System evaluates a fixed set of matrics in both the domains to first get a score for the individual matrics, then combine them to create the domain rating, then combine the domain ratings to get the overall rating." +
                "\n\nThe Metrics used for the System and User Settings Security Evaluation are:" +
                "\n1. Is the device rooted." +
                "\n2. Is Encrption enabled on the device" +
                "\n3. Is USB Debugging Enabled on the Device" +
                "\n4. Is Device Lock Enabled" +
                "\n5. The Version of the OS that the device is running" +
                "\n\nThe Metrics For the Application Security Evaluation are:" +
                "\n1. Number of Downloads" +
                "\n2. Developer Reliability (using Badges)" +
                "\n3. Application Rating (0 to 5, 5 being maximum)" +
                "\n4. Latest Release/Update date for the App in the Market" +
                "\n5. Number of Ratings" +
                "\n6. Third Party Apps" +
                "\n\nThe data acquired by each of these metrics is treated as a fact and is stored in a knowledge base. A set of rules are then executed over these facts to get the evaluations." +
                "\nIn the way stated above,the evaluation scores are first scaled appropriately and then added to create an overall security rating." +
                "\n\nBoth the Domains Contribute equally towards the final score." +
                "\n\n\nThe Results are Displayed in a separate page accessible by tapping the button 'View Report'." +
                "\nHere, first the System Report is displayed for the Settings that need the user's attention to make the device more secure." +
                "\nNext, In the Application Report, the third party apps are displayed." +
                "\nAnd finally the Security evaluation of every individual application is displayed." +
                "\nDue to the API limitations, the Evaluation may take up to 60 seconds depending on the number of applications installed on the device.";
        TextView toverall=(TextView)findViewById(R.id.textView12);
        toverall.setText(Instruct);
    }
}
