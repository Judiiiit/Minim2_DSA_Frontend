package com.example.android_proyecto.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_proyecto.Models.IssueRequest;
import com.example.android_proyecto.R;
import com.example.android_proyecto.RetrofitClient;
import com.example.android_proyecto.Services.ApiService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportIssueActivity extends AppCompatActivity {

    private EditText etDate, etInformer, etMessage;
    private Button btnSendIssue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        etDate = findViewById(R.id.etDate);
        etInformer = findViewById(R.id.etInformer);
        etMessage = findViewById(R.id.etMessage);
        btnSendIssue = findViewById(R.id.btnSendIssue);

        String dateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
        etDate.setText(dateNow);

        btnSendIssue.setOnClickListener(v -> sendIssue());
    }

    private void sendIssue() {
        String date = etDate.getText().toString().trim();
        String informer = etInformer.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (date.isEmpty() || informer.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Fill date, informer and message", Toast.LENGTH_SHORT).show();
            return;
        }

        IssueRequest req = new IssueRequest(date, informer, message);

        ApiService api = RetrofitClient.getApiService();
        api.postIssue(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ReportIssueActivity.this, "Issue sent!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ReportIssueActivity.this,
                            "Error sending issue (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ReportIssueActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
