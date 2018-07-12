package com.twilio.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for {@link RequestValidator}.
 */
public class RequestValidatorTest {
    private Map<String, String> params = new HashMap<>();
    private String url = "https://mycompany.com/myapp.php?foo=1&bar=2";
    private RequestValidator validator = new RequestValidator("12345");
    private String signature = "RSOYDt4T1cUTdK1PDd93/VVr8B8=";
    private String body = "{\"property\": \"value\", \"boolean\": true}";
    private String bodyHash = "Ch/3Y02as7ldtcmi3+lBbkFQKyg6gMfPGWMmMvluZiA=";
    private String bodyHashEncoded = bodyHash.replace("+", "%2B").replace("=", "%3D");

    @Before
    public void setUp() {
        params.put("Digits", "1234");
        params.put("CallSid", "CA1234567890ABCDE");
        params.put("To", "+18005551212");
        params.put("Caller", "+14158675309");
        params.put("From", "+14158675309");
    }

    @Test
    public void testValidate() {
        Assert.assertTrue("Request does not match provided signature", validator.validate(url, params, signature));
    }

    @Test
    public void testFailsWhenIncorrect() {
        signature = "NOTRSOYDt4T1cUTdK1PDd93/VVr8B8=";
        boolean isValid = validator.validate(url, params, signature);

        Assert.assertFalse("Request should have failed validation, but didn't", isValid);
    }

    @Test
    public void testValidateBody() {
        boolean isValid = validator.validateBody(body, bodyHash);

        Assert.assertTrue("Body validation failed", isValid);
    }

    @Test
    public void testValidateBodyFailsWhenWrong() {
        boolean isValid = validator.validateBody(body, "WRONG");

        Assert.assertFalse("Body validation should have failed, but didn't", isValid);
    }

    @Test
    public void testValidateWithBody() throws URISyntaxException {
        String url = this.url + "&bodySHA256=" + bodyHashEncoded;
        String signatureWithHash = "afcFvPLPYT8mg/JyIVkdnqQKa2s=";
        boolean isValid = validator.validate(url, body, signatureWithHash);

        Assert.assertTrue("Body validation failed", isValid);
    }

    @Test
    public void testValidateWithNoOtherParameters() throws URISyntaxException {
        String url = "https://mycompany.com/myapp.php?bodySHA256=" + bodyHashEncoded;
        String signatureWithHash = "DXnNFCj8DJ/hZmiSg4UzaDHw5Og=";
        boolean isValid = validator.validate(url, body, signatureWithHash);

        Assert.assertTrue("Body validation failed", isValid);
    }

    @Test
    public void testValidateBodyWithoutSignature() throws URISyntaxException {
        boolean isValid = validator.validate(url, body, signature);

        Assert.assertFalse("Validation should have failed with no bodySHA256", isValid);
    }

}
