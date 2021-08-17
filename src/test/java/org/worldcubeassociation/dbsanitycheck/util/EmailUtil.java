package org.worldcubeassociation.dbsanitycheck.util;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.List;

public final class EmailUtil {
    private EmailUtil() {
    }

    public static String getEmailResult(List<MimeMessage> receivedMessages) throws MessagingException, IOException {
        // Email content is just the first email of the current run
        MimeMultipart mimeMultipart = (MimeMultipart) receivedMessages.get(0).getContent();
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
        if (bodyPart.getContent() instanceof MimeMultipart) {
            result = getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
        } else if (bodyPart.isMimeType("text/plain")) {
            result = (String) bodyPart.getContent();
        } else if (bodyPart.isMimeType("text/html")) {
            String html = (String) bodyPart.getContent();
            result = html;
        }
        return result;
    }
}
