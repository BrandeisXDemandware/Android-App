package com.demanddev.brandeis.dw;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;


/**
 * Created by jingzou on 10/12/15.
 */
public class WelcomeActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.registerButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // No account, load new account view
                        Intent intent = new Intent(WelcomeActivity.this,
                                RegisterActivity.class);
                        startActivityForResult(intent, 0);
                    }
                });

        findViewById(R.id.loginButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Existing Account, load login view
                        Intent intent = new Intent(WelcomeActivity.this,
                                LoginActivity.class);
                        startActivityForResult(intent, 0);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        finish();
    }
}
