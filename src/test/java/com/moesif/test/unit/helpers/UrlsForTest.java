package com.moesif.test.unit.helpers;

public class UrlsForTest {
    public static final String URL_404_EMPTY_JSON = "https://jsonplaceholder.typicode.com/posts/employees/6";
    public static final String URL_200_FULL_JSON_NO_CONT_LEN_RESP = "https://jsonplaceholder.typicode.com/users";
    public static final String URL_200_IP = "https://8.8.8.8/";
    public static final String URL_200_HTTP_HTML_NO_PATH = "http://dns.google/";
    public static final String URL_200_HTTP_TEXT ="https://httpstat.us/200";
    public static final String URL_404 = "https://google.com/expect-404";
    public static final String URL_DOMAIN_NOT_EXIST = "https://domain.not.exist/expect-no-domain";
    public static final String URL_POST_200_JSON = "https://jsonplaceholder.typicode.com/posts/1";
    public static final String SAMPLE_JSON_BODY = "{\"uid\":1, \"title\":\"hello\\n there\"}";

    // basic auth url, user, pwd from OkHttp recipe:
    // https://square.github.io/okhttp/recipes/#handling-authentication-kt-java
    public static final String URL_BASIC_AUTH = "http://publicobject.com/secrets/hellosecret.txt";
    public static final String BASIC_AUTH_USER = "jesse";
    public static final String BASIC_AUTH_PWD = "password1";

}
