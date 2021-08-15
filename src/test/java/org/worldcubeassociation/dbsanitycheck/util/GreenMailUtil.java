package org.worldcubeassociation.dbsanitycheck.util;

import com.icegreen.greenmail.junit5.GreenMailExtension;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;

public final class GreenMailUtil {
    private GreenMailUtil() {
    }

    public static String getEmailResult(GreenMailExtension greenMail) throws MessagingException, IOException {
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

        // Email content is just the first email of the current run
        MimeMultipart mimeMultipart = (MimeMultipart) receivedMessages[0].getContent();

        return getTextFromMimeMultipart(mimeMultipart);

    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws IOException, MessagingException {
        // Adapted from
        // https://stackoverflow.com/questions/11240368/how-to-read-text-inside-body-of-mail-using-javax-mail

        int count = mimeMultipart.getCount();
        if (count == 0)
            throw new MessagingException("Multipart with no body parts not supported.");
        boolean multipartAlt = new ContentType(mimeMultipart.getContentType()).match("multipart/alternative");
        if (multipartAlt)
            // alternatives appear in an order of increasing
            // faithfulness to the original content. Customize as req'd.
            return getTextFromBodyPart(mimeMultipart.getBodyPart(count - 1));

        // Index 0 is the email part
        BodyPart bodyPart = mimeMultipart.getBodyPart(0);
        return getTextFromBodyPart(bodyPart);
    }

    private static String getTextFromBodyPart(
            BodyPart bodyPart) throws IOException, MessagingException {

        String result = "";
        if (bodyPart.isMimeType("text/plain")) {
            result = (String) bodyPart.getContent();
        } else if (bodyPart.isMimeType("text/html")) {
            String html = (String) bodyPart.getContent();
            result = html;
        } else if (bodyPart.getContent() instanceof MimeMultipart) {
            result = getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
        }
        return result;
    }
}
