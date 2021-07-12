package com.example.demo_rashdriving;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class otp extends AppCompatActivity {


    private TextView mobile;
    private Button cont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_otp);

        mobile = findViewById(R.id.mobile);
        cont = findViewById(R.id.login);

        cont.setOnClickListener(new View.OnClickListener() {

            private String phone;

            @Override
            public void onClick(View v) {
                phone = mobile.getText().toString().trim();
                if (phone.isEmpty() || phone.length()<10)
                {
                    mobile.setError("Enter Valid no");
                    mobile.requestFocus();
                    return;
                }
                Intent intent=new Intent(otp.this,phoneverifyy.class);
                intent.putExtra("mobile", phone);
                startActivity(intent);

            }

        });

    }
}
