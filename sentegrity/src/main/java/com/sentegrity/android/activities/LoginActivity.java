package com.sentegrity.android.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.sentegrity.android.R;
import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.policy.SentegrityPolicy;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        startAnalyzing();
    }

    private void startAnalyzing(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Analyzing");
        progressDialog.setMessage("Mobile Security Posture");
        progressDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.cancel();
                showError();

                Log.d("coreDetection", "policy start parse");
                SentegrityPolicy policy = CoreDetection.getInstance().parsePolicy("default.policy");
                Log.d("coreDetection", "policy end parse");
            }
        }, 1000);
    }

    private void showError() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        final View view = LayoutInflater.from(this).inflate(R.layout.dialog_login_error_password, null);
        final EditText password = (EditText) view.findViewById(R.id.password);

        dialogBuilder.setView(view);

        dialogBuilder.setTitle("High Risk Device");
        dialogBuilder.setMessage("Some problem may have occurred. This attempt has been recorded.\n\n Enter password to continue.");
        dialogBuilder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                finishAffinity();
            }
        });
        dialogBuilder.setNegativeButton("View Issues", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                finishAffinity();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.setCancelable(false);
        b.show();
    }

}
