# Moesif Java OkHttp Interceptor SDK  

 [![Built For][ico-built-for]][link-built-for]
 [![Latest Version][ico-version]][link-package]
 [![Software License][ico-license]][link-license]
 [![Source Code][ico-source]][link-source]
  
  
## Introduction  
  
`moesif-java-okhttp-interceptor` is a Java OkHttp interceptor that logs outbound HTTP(s) calls and sends events to [Moesif](https://www.moesif.com) for API analytics and monitoring    .  

The SDK includes `Java` and `Kotlin` examples. It is implemented as a [OkHttp Interceptor](https://square.github.io/okhttp/interceptors/)  
and can be used either as `Application Interceptor ` or `Network Interceptor`. It requires `Moesif Application Id` credentials to submit events to Moesif.  

With a single statement `.addInterceptor(new MoesifOkHttp3Interceptor())` it can start capturing events.  

[Source Code and samples on GitHub](https://github.com/Moesif/moesif-java-okhttp-interceptor)  

## How to install  
For Maven users, add dependency to your `pom.xml`: 
  
```xml  
<dependency>
	<groupId>com.moesif</groupId>
	<artifactId>moesif-okhttp-interceptor</artifactId>
	<version>1.0.4</version>
</dependency> 
```  
For Gradle users, add to your project's build.gradle file:  
  
```gradle  
repositories {  
  dependencies {     
    implementation 'com.moesif:moesif-okhttp-interceptor:1.0.4'
}
```  

## How to use  
Set the Moesif Application Id environment variable. Alternatively, this key can also be directly passed using `MoesifApiConnConfig`.  
  
```bash  
$ export MOESIF_APPLICATION_ID = "Your Moesif App Id Here"  
```  
Build the OkHttp client.  
  
The Moesif OkHttp Interceptor can be utilized for both types of [OkHttp interceptors Link](https://square.github.io/okhttp/interceptors/)  
  
| |  Application Interceptor |  
|---|---|  
| Java  | `addInterceptor(new MoesifOkHttp3Interceptor())`  |
| Kotlin |`addInterceptor(MoesifOkHttp3Interceptor())` |  
  
For Network Interceptor, change `addInterceptor` to `addNetworkInterceptor`  
  
To pass `Moesif Application Id` directly in lieu of setting as environment variable:  
Change `MoesifOkHttp3Interceptor()` to `MoesifOkHttp3Interceptor("Your Moesif Application Id here")`   
The [Official OkHttp3 recipe for Synchronous Get](https://square.github.io/okhttp/recipes/#synchronous-get-kt-java)  
has been modified below.  
  
### Example (Kotlin)  
  
```bash  
$ export MOESIF_APPLICATION_ID = "Your Moesif App Id Here"  
```  
  
```kotlin  
import com.moesif.sdk.okhttp3client.MoesifOkHttp3Interceptor

val client = OkHttpClient.Builder()
 .addInterceptor(MoesifOkHttp3Interceptor()) // The only modification to official sample
 .build()

val request = Request.Builder()  
 .url("https://publicobject.com/helloworld.txt")
 .build()  

client.newCall(request).execute().use { response ->  
  if (!response.isSuccessful) throw IOException("Unexpected code $response")  
  for ((name, value) in response.headers) {
    println("$name: $value")
  }
println(response.body!!.string())}
```  
  
### Example (Java)  
  
```bash  
$ export MOESIF_APPLICATION_ID = "Your Moesif App Id Here"  
```  
```java  
import com.moesif.sdk.okhttp3client.MoesifOkHttp3Interceptor;  
  
private final OkHttpClient client = new OkHttpClient.Builder()  
 .addInterceptor(new MoesifOkHttp3Interceptor())  // The only modification to official sample
 .build();  

public void run() throws Exception {  
 Request request = new Request.Builder()
   .url("https://publicobject.com/helloworld.txt")
   .build();  
 try (Response response = client.newCall(request).execute()) {
   if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
   Headers responseHeaders = response.headers();
   for (int i = 0; i < responseHeaders.size(); i++) {
      System.out.println(responseHeaders.name(i) + ": " +  responseHeaders.value(i));
   }
   System.out.println(response.body().string()); 
   }
}  
```  
  
## Obtaining your Moesif Application Id  
Your Moesif Application Id can be found in the [_Moesif Portal_](https://www.moesif.com/).  
After signing up for a Moesif account, your Moesif Application Id will be displayed during the onboarding steps.
  
You can always find your Moesif Application Id at any time by logging   
into the [_Moesif Portal_](https://www.moesif.com/), click on the top right menu,  
and then clicking _Installation_.  
  
## Advanced Configuration options

***Filtering, masking and customizing events sent to Moesif***

By default, all events are submitted, and no content is masked.
This default behavior is captured in the default config file `DefaultEventFilterConfig.java`

This behavior can be overwritten by creating a custom config class that implements `IInterceptEventFilter`.

Here is a sample customized config file "MyCustomEventFilterConfig.java"
```java
public static class MyCustomEventFilterConfig implements IInterceptEventFilter{

    @Override
    public EventModel maskContent(EventModel eventModel) {
        if (eventModel.getRequest().getIpAddress() == "127.0.0.1")
            eventModel.getRequest().setIpAddress("0.0.0.0");
        return eventModel;
    }

    @Override
    public boolean skip(Request request, Response response) {
        return request.method() == "DELETE";
    }

    @Override
    public Optional<String> identifyUser(Request request, Response response) {
        return Optional.of("customUser");
    }

    @Override
    public Optional<String> identifyCompany(Request request, Response response) {
        return Optional.of("customCompany");
    }

    @Override
    public Optional<String> sessionToken(Request request, Response response) {
        return Optional.of("customSessionToken");
    }

    @Override
    public @Nullable Map<String, Object> getMetadata(Request request, Response response) {
        Map<String, Object> customMetadata = new HashMap<String, Object>();
        Map<String, Object> subObject = new HashMap<String, Object>();
        subObject.put("destructive_method", request.method() == "DELETE");
        customMetadata.put("cost_center", "a554411");
        customMetadata.put("retention_months", 12);
        customMetadata.put("method_detais", subObject);
        return customMetadata;
    }

    @Override
    public Optional<String> getApiVersion(Request request, Response response) {
        return Optional.of("v-3.1415");
    }
}
```
To use this custom config, update it prior to constructing the interceptor
```java
MoesifApiConnConfig cfg = new MoesifApiConnConfig();
cfg.setEventFilterConfig(new MyCustomEventFilterConfig());
MoesifOkHttp3Interceptor interceptor = new MoesifOkHttp3Interceptor(cfg);
```

***Batch size of events submitted to Moesif***

By default, events submitted asynchronously to Moesif are batched in sizes of `5` as configured in `MoesifApiConnConfig.java`
To configure the batch size to any value greater than zero:
```java
int customSize = 20; // submit events in batch of size 20
new MoesifOkHttp3Interceptor(customSize)
``` 
OR
```java
MoesifApiConnConfig cfg = new MoesifApiConnConfig();
cfg.setEventsBufferSize(customSize);
new MoesifOkHttp3Interceptor(cfg);
```
Per current implementation, the batch has to be full to send events. Minimum batch size is 1 - which will send individual events immediately. The buffered events are not modified based on aging of these events.

Note that this events buffer is in memory. It is lost if the process is ended. It is emptied every time events are submitted to Moesif. Setting the buffer to be large can also increase memory consumption.

The events are submitted asynchronously to Moesif. So if the program ends immediately after making an OkHttp request, the event may not have been submitted to Moesif even if the event buffer is size `1`. In such case, adding a slight delay may help eg: `TimeUnit.SECONDS.sleep(3);`

### Credits:  
`com.moesif.external.facebook.stetho.inspector.network` contains code borrowed (and modified for Moesif) from Facebook/Stetho [Official site](https://facebook.github.io/stetho/) | [Code on Github](https://github.com/facebook/stetho)  
*Thank you Facebook/Stetho!*  
  
### Built for OkHttp 
[Official Readme](https://square.github.io/okhttp/) | [Code on Github](https://github.com/square/okhttp)  
*Thank you Square/OkHttp!*

[ico-built-for]: https://img.shields.io/badge/okhttp-OkHttp%20Client-green
[ico-version]: https://img.shields.io/maven-central/v/com.moesif/moesif-okhttp-interceptor
[ico-license]: https://img.shields.io/badge/License-Apache%202.0-green.svg
[ico-source]: https://img.shields.io/github/last-commit/moesif/moesif-servlet.svg?style=social

[link-built-for]: https://square.github.io/okhttp
[link-package]: https://search.maven.org/artifact/com.moesif/moesif-okhttp-interceptor
[link-license]: https://raw.githubusercontent.com/Moesif/moesif-java-okhttp-interceptor/master/LICENSE
[link-source]: https://github.com/moesif/moesif-java-okhttp-interceptor
