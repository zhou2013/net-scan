/*
 * @(#) EncodeUtils.java 2014-7-4
 * 
 * Copyright 2013 NetEase.com, Inc. All rights reserved.
 */
package zzhao.network;

import java.io.IOException;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

/**
 *
 * @author hzzhaozhou
 * @version 2014-7-4
 */
public class EncodeUtils {

    public static String detectEncode(byte[] buf) throws IOException {
        final EncodeResult result = new EncodeResult();
        nsDetector det = new nsDetector(nsDetector.ALL);
        nsICharsetDetectionObserver cdo = new nsICharsetDetectionObserver() {
            public void Notify(String charset) {
                result.found = true;
                result.encode = charset;
            }
        };
        det.Init(cdo);
        boolean done = false;
        boolean isAscii = true;

        if (isAscii)
            isAscii = det.isAscii(buf, buf.length);
        // DoIt if non-ascii and not done yet.
        if (!isAscii && !done)
            done = det.DoIt(buf, buf.length, false);

        det.DataEnd();

        if (isAscii) {
            result.encode = "ASCII";
            result.found = true;
        }

        if (!result.found) {
            String prob[] = det.getProbableCharsets();
            if (prob.length > 0) {
                // 在没有发现情况下，则取第一个可能的编码
                result.encode = prob[0];
            } else {
                return null;
            }
        }
        return result.encode;
    }

    private static class EncodeResult {
        private boolean found = false;
        private String encode = null;
    }
}
