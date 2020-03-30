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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Upload {

    private String fileName;
    private String fileData;

    Upload(final String fileName, final String fileData) {
        this.fileName = fileName;
        this.fileData = fileData;
    }

    static String getDataString(Map<String, String> params) throws UnsupportedEncodingException {
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
        StringBuilder body = new StringBuilder();

        String guid = "123456789837261";
        String border = "--------" + guid + '\n';

        body.append(border);

        body.append("Content-Disposition: form-data; name=\"files[]\"; filename=\"");
        body.append(fileName);
        body.append('\n');

        body.append("Content-Type: text/csv\n");
        body.append('\n');
        body.append(fileData);
        body.append('\n');
        body.append(border);

        return body.toString();
    }


//        -----------------------------377379154022942282103505584138
//        Content-Disposition: form-data; name="type"
//
//        composer
//                -----------------------------377379154022942282103505584138
//        Content-Disposition: form-data; name="files[]"; filename="members.csv"
//        Content-Type: text/csv
//
//        Name,User Name,Phone #,Neighborhood,Address,Consumer,Dispatcher,Driver,
//                stone,stone,510 526-6895,Grizzly Peak,1020 Creston Road,false,false,false,
//                Michel Thouati,MichelThouati,262.434.0554,Grizzly Peak,1030 Creston Rd Berkeley CA 94708,false,false,false,
//                Mary Pat Farrell,MPFarrell,2623036233,Grizzly Peak,1030 Creston Road,false,true,false,
//                Ariel Shemtov,ArielS,6282247997,Grizzly Peak,1030 Creston rd Berkeley CA 94708,false,false,false,
//                Kaelan Thouati de Tazoult,KaelanTdT,510.650.2299,Grizzly Peak,1030 Creston Rd Berkeley CA 94708,false,true,false,
//                Test User 2,test-user-2,510 555-1213,Glendale - La Loma Park,12345 La Loma ,false,false,false,
//                Test User 1,test-user-1,510 555-1212,Keeler Ave,722 3/4 #6 Keeler Ave,false,false,false,
//
//                -----------------------------377379154022942282103505584138--

    public static void main(String[] args) throws UnsupportedEncodingException {

        Map<String, String> params = new HashMap<>();
        params.put("files[]", "this is my file data");

        Upload upload = new Upload("x.cvs", "aaa\nbbb\naaa\nbbb\n");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://helpberkeley.org/uploads.json"))
                .header("Api-Username", "fred")
                .header("Api-Key", "berfle")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(upload.generateBody()))
                .build();

        System.out.println(getDataString(params));


    }
}
