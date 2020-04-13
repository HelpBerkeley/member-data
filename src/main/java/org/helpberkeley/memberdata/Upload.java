/******************************************************************************
 * Copyright (c) 2020 helpberkeley.org
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package org.helpberkeley.memberdata;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

// FIX THIS, DS: WIP try to reverse-engineer upload

public class Upload {

    private final String fileName;
    private final String fileData;

    Upload(final String fileName, final String fileData) {
        this.fileName = fileName;
        this.fileData = fileData;
    }

    static String getDataString(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return result.toString();
    }
//        $contentType=$r.BaseResponse.ContentType
//        $content=Get-Content $fileOut
//        $boundary=[system.Guid]::NewGuid().ToString()
//        $clientID=[system.Guid]::NewGuid().ToString()
//        $body=(
//                "--$boundary",
//                'Content-Disposition: form-data; name="type"',
//                '',
//                "image",
//                "--$boundary",
//                "Content-Disposition: form-data; name=""files[]"" filename=$imageName",
//                "Content-Type: ""$contentType""",
//                '',
//                "$content",
//                "--$boundary--"
//) -join "`r`n"
//        $tempURL=$urlTo+"uploads.json"+$loginTo+"&client_id=$clientID&synchronous=1"
//        $resp=Invoke-WebRequest $tempURL -Method Post -ContentType "multipart/form-data; boundary=$boundary" -Body $body -Verbose


    String generateBody() {

        String guid = "123456789837261";
        String border = "--------" + guid + '\n';

        String body = border +
                "Content-Disposition: form-data; name=\"files[]\"; filename=\"" +
                fileName +
                '\n' +
                "Content-Type: text/csv\n" +
                '\n' +
                fileData +
                '\n' +
                border;
        return body;
    }


    public static HttpRequest.BodyPublisher ofMimeMultipartData(Map<Object, Object> data,
                                                                String boundary) throws IOException {
        var byteArrays = new ArrayList<byte[]>();
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
                .getBytes(StandardCharsets.UTF_8);
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            byteArrays.add(separator);

            if (entry.getValue() instanceof Path) {
                var path = (Path) entry.getValue();
                String mimeType = Files.probeContentType(path);
                byteArrays.add(("\"" + entry.getKey() + "\"; filename=\"" + path.getFileName()
                        + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                byteArrays.add(Files.readAllBytes(path));
                byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
            }
            else {
                byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n")
                        .getBytes(StandardCharsets.UTF_8));
            }
        }
        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    public static HttpRequest.BodyPublisher uploadBody(String boundary) {

        var byteArrays = new ArrayList<byte[]>();
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
                .getBytes(StandardCharsets.UTF_8);
        byteArrays.add(separator);

        String mimeType = "test/csv";
        byteArrays.add(("\"files[]\"; filename=\"" + "test.csv"
                + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrays.add(new String("a,b,c\n1,2,3\n").getBytes());
        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));

        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }
}
