package com.sentegrity.android.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.sentegrity.android.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showError();
            }
        }, 1000);
    }

    private void showError() {
        Dialog dialog = new Dialog(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_login_error, null);
        dialog.setContentView(dialogView);

        dialogView.findViewById(R.id.login).setOnClickListener(this);
        dialogView.findViewById(R.id.view_issues).setOnClickListener(this);

        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login:
                startActivity(new Intent(this, DashboardActivity.class));
                break;
            case R.id.view_issues:
                startActivity(new Intent(this, DashboardActivity.class));
                break;
        }
    }
}
