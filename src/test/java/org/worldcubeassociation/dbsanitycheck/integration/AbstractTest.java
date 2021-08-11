package org.worldcubeassociation.dbsanitycheck.integration;

import com.google.common.base.CaseFormat;
import org.apache.commons.io.FileUtils;
import org.worldcubeassociation.dbsanitycheck.util.LoadResourceUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.commons.function.Try.success;

public abstract class AbstractTest {
    private static final int MAX_PAYLOAD_LEN = 50;

    public static void validateHtmlResponse(String actualHtmlContent) {

        String htmlWrapper = LoadResourceUtil.getResource("template/html-wrapper.html");
        String actualHtmlWrappedContent = String.format(htmlWrapper, actualHtmlContent);

        StackWalker walker = StackWalker.getInstance();
        StackWalker.StackFrame stackFrameOptional = walker.walk(stream -> stream
                .filter(f -> f.getClassName().contains("org.worldcubeassociation.dbsanitycheck.integration.service"))
                .findFirst()).orElseThrow(() -> new RuntimeException("Cant find test StackFrame"));

        final String methodName = stackFrameOptional.getMethodName();
        final String fullClassName = stackFrameOptional.getClassName();
        final String className = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN,
                fullClassName.substring(fullClassName.lastIndexOf(".") + 1));

        String resource = String.format("responses/%s/%s.html", className, methodName);

        try {

            final String expectedHtmlWrappedContent = LoadResourceUtil.getResource(resource);

            assertEquals(expectedHtmlWrappedContent, actualHtmlWrappedContent);
        } catch (UncheckedIOException ex) {
            if (ex.getCause() instanceof FileNotFoundException) {
                try {
                    File file = new File("src/test/resources/" + resource);
                    FileUtils.touch(file);
                    FileUtils.writeStringToFile(file, actualHtmlWrappedContent, StandardCharsets.UTF_8, false);
                    success("Test file did not exist. File created and test succeeded.");

                } catch (Exception e) {
                    throw new TestCasePayloadGeneratedException(actualHtmlWrappedContent, resource);
                }
            }
        }
    }

    static class TestCasePayloadGeneratedException extends RuntimeException {
        public TestCasePayloadGeneratedException(String payload, String resource) {
            super(String.format("Resource \"%s\" not found for this execution." +
                                    " So resources was generated based on this \"%s %s\" payload." +
                                    " Run tests again!", resource
                            , payload.substring(0, Math.min(payload.length(), MAX_PAYLOAD_LEN))
                            , payload.length() > MAX_PAYLOAD_LEN ? "..." : ""
                    )
            );
        }
    }
}
