package com.example.btlt7nguyenxuanhai;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText edtUrl;
    private Button btnDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtUrl = findViewById(R.id.edtUrl);
        btnDownload = findViewById(R.id.btnDownload);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        btnDownload.setOnClickListener(v -> {
            String url = edtUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập URL hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, DownloadService.class);
            intent.putExtra("url", url);
            startService(intent);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền hiển thị thông báo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không có quyền hiển thị thông báo", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
