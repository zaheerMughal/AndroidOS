package com.android.os;

public class Message {

    private String senderNumber;
    private String messageBody;

    public Message() {
        this.senderNumber = "Null...";
        this.messageBody = "Null...";
    }

    public String getSenderNumber() {
        return senderNumber;
    }

    public void setSenderNumber(String senderNumber) {
        this.senderNumber = senderNumber;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }
}
