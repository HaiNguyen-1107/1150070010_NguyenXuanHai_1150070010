package com.example.btlt7nguyenxuanhai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Toast.makeText(context, "Nhận action: " + action, Toast.LENGTH_SHORT).show();
        // TODO: có thể thêm logic Pause/Resume thực sự nếu muốn
    }
}
