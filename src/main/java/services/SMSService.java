package services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SMSService {



    // Vos identifiants Twilio
    private static final String ACCOUNT_SID = "AC964013b28b281d98399d55acc44fc368";
    private static final String AUTH_TOKEN = "7ce34845284866a3d3117aadaff5f9f0";
    private static final String FROM_NUMBER = "+19856165928";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static void sendSMS(String toNumber, String messageBody) {
        Message message = Message.creator(
                        new PhoneNumber(toNumber),
                        new PhoneNumber(FROM_NUMBER),
                        messageBody)
                .create();

        System.out.println("SMS envoyé avec l'ID: " + message.getSid());
    }
}
