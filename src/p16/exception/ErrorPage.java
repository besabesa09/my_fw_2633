package p16.exception;

public class ErrorPage {
    public static String doError(String message) {
        String error = 
        "<!DOCTYPE html>\r\n" + //
        "<html lang='en'>\r\n" + //
        "<head>\r\n" + //
        "    <meta charset='UTF-8'>\r\n" +
        "    <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>\r\n" +
        "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\r\n" +
        "    <title>Error page</title>\r\n" + //
        "    <style>\r\n" + //
        "        body {\r\n" + //
        "            display: flex;\r\n" + //
        "            justify-content: center;\r\n" + //
        "            align-items: center;\r\n" + //
        "            height: 100vh;\r\n" + //
        "            margin: 0;\r\n" + //
        "            background-color: #f0f0f0;\r\n" + //
        "            font-family: Arial, sans-serif;\r\n" + //
        "            color: #333;\r\n" + //
        "        }\r\n" + //
        "        .error {\r\n" + //
        "            padding: 40px;\r\n" + //
        "            border: 2px solid #ff6f61;\r\n" + //
        "            border-radius: 10px;\r\n" + //
        "            background-color: #fff;\r\n" + //
        "        }\r\n" + //
        "        h1 {\r\n" + //
        "            font-size: 36px;\r\n" + //
        "            margin-bottom: 10px;\r\n" + //
        "            color: #ff6f61;\r\n" + //
        "        }\r\n" + //
        "        p {\r\n" + //
        "            font-size: 18px;\r\n" + //
        "            color: #555;\r\n" + //
        "        }\r\n" + //
        "    </style>\r\n" + //
        "</head>\r\n" + //
        "<body>\r\n" + //
        "    <div class='error'>\r\n" + //
        "        <h1>Erreur</h1>\r\n" + //
        "        <p>" + message + "</p>\r\n" + //
        "    </div>\r\n" + //
        "</body>\r\n" + //
        "</html>";
        return error;
    }
}
