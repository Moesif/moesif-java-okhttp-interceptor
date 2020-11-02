package com.moesif.sdk.okhttp3client.config;

import java.util.Arrays;
import java.util.List;

public class DefaultDomainData {
    // refer
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
    /**
     * Default list of body content types that are whitelisted to be submitted
     * to collector
     * We are currently going with blacklist approach
     */
    public static final List<String> bodyContentTypesWhiteList =
            Arrays.asList(
                    "application/json",
                    "application/ld+json",
                    "application/rtf",
                    "application/x-httpd-php",
                    "application/xhtml+xml",
                    "application/xml",
                    "text/calendar",
                    "text/css",
                    "text/csv",
                    "text/html",
                    "text/javascript",
                    "text/plain"
            );

    /**
     * Blacklisted Body Content types
     * ALL LOWER CASE ONLY
     */
    public static final List<String> bodyContentTypesBlackList =
            Arrays.asList(
                    "application/epub+zip",
                    "application/gzip",
                    "application/java-archive",
                    "application/msword",
                    "application/octet-stream",
                    "application/ogg",
                    "application/pdf",
                    "application/vnd.amazon.ebook",
                    "application/vnd.apple.installer+xml",
                    "application/vnd.mozilla.xul+xml",
                    "application/vnd.ms-excel",
                    "application/vnd.ms-fontobject",
                    "application/vnd.ms-powerpoint",
                    "application/vnd.oasis.opendocument.presentation",
                    "application/vnd.oasis.opendocument.spreadsheet",
                    "application/vnd.oasis.opendocument.text",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.rar",
                    "application/vnd.visio",
                    "application/x-7z-compressed",
                    "application/x-abiword",
                    "application/x-bzip",
                    "application/x-bzip2",
                    "application/x-csh",
                    "application/x-freearc",
                    "application/x-sh",
                    "application/x-shockwave-flash",
                    "application/x-tar",
                    "application/zip",
                    "audio/3gpp",
                    "audio/3gpp2",
                    "audio/aac",
                    "audio/midi audio/x-midi",
                    "audio/mpeg",
                    "audio/ogg",
                    "audio/opus",
                    "audio/wav",
                    "audio/webm",
                    "font/otf",
                    "font/ttf",
                    "font/woff",
                    "font/woff2",
                    "image/bmp",
                    "image/gif",
                    "image/jpeg",
                    "image/png",
                    "image/svg+xml",
                    "image/tiff",
                    "image/vnd.microsoft.icon",
                    "image/webp",
                    "video/3gpp",
                    "video/3gpp2",
                    "video/mp2t",
                    "video/mpeg",
                    "video/ogg",
                    "video/webm",
                    "video/x-msvideo"
            );

    public static Long maxAllowedBodyBytesRequest = 1024 * 100L; // 1MB for req + resp combined

    public static Long maxAllowedBodyBytesResponse = 1024 * 100L; // 1MB for req + resp combined
}
