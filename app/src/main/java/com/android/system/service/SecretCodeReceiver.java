package com.android.system.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SecretCodeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SECRET_CODE")) {
            // When *#*#1234#*#* is dialed, open the VM
            Intent i = new Intent(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
