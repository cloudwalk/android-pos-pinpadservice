# Pinpad Service

<img src="SHIELD.svg"/><br/>

Originally built as a (P)roof (O)f (C)oncept, the Pinpad Service serves as a
virtual ABECS PINPAD for POS solutions.  
From the entire ABECS specification, multimedia, proprietary and deprecated
instructions are left out, reducing scope to:  

- OPN, GIX and CLX
- CEX, CHP, EBX, GCD, GPN, GTK, MNU and RMC
- TLI, TLR and TLE
- GCX, GED, GOX and FCX

Be sure to check [ABECS](https://www.abecs.org.br/) website and
[specification](https://www.abecs.org.br/certificacao-funcional-dos-pinpads) to
have a deeper understanding on how to make requests and handle its responses.  

Made available in its own installable package, the Pinpad Service can be shared
by several applications and updated independently. The Library that pairs with
it helps to hide the complexity of handling the service, as well as AIDL
restrictions (check the [DEMO application](#demo-application) for an example on
how to consume the Pinpad Service using the associated Pinpad Library).  

## Project dependencies

Each platform covered by this project may have its own dependencies, each in
its own module. They'll be listed in their respective `build.gradle` file -
e.g. [Verifone/build.gradle](VerifoneService/build.gradle).  
For instance, these are the Verifone Pinpad Service v1.1.5 dependencies:  

```gradle
dependencies {
    implementation 'androidx.appcompat:appcompat:1.4.1'
    implementation 'com.google.android.material:material:1.5.0'
    implementation 'org.jetbrains:annotations:16.0.1'

    implementation 'io.cloudwalk:loglibrary:1.1.4'                  // local dependency
    implementation 'io.cloudwalk:utilitieslibrary:1.0.12'           // local dependency

    debugImplementation files('libs/PPCompX990-v001.29_debug.aar')  // copyrighted dependency
    releaseImplementation files('libs/PPCompX990-v001.29.aar')      // copyrighted dependency

    implementation project(path: ':PinpadLibrary')
}
```

### Local dependencies

Local dependencies are those within the scope of the Pinpad Service development
team. They need to be made available before the service can be built:  

1. Clone the repositories
   [android-misc-loglibrary](https://github.com/mauriciospinardi/android-misc-loglibrary)
   and [android-misc-utilitieslibrary](https://github.com/mauriciospinardi/android-misc-utilitieslibrary)
2. Follow the instructions in each README.md to locally publish them.

### Copyrighted dependencies

Copyrighted dependencies are those provided by third parties. They are under
NDA and can't be made public.  
For each platform covered by this project, reach out the respective platform
representatives[^1].  
For each platform module, create a new `libs` directory and save the
correspondent dependencies in it.

[^1]: Those with CloudWalk credentials will find the entire set of dependencies
under NDA through a single link:
[CloudWalk Devices](https://drive.google.com/drive/folders/1KX-WcBStMcyAN9CY-LTCBJ9zlkAlfEVA)

## DEMO application

The Pinpad Service includes a DEMO application, covering the very basics:  

- [SplashActivity.java](DEMO/src/main/java/io/cloudwalk/pos/demo/presentation/SplashActivity.java)
  shows how to bind the service using the Pinpad Library.
- [MainActivity.java](DEMO/src/main/java/io/cloudwalk/pos/demo/presentation/MainActivity.java)
  shows how to perform local requests.
  - _Check [DEMO.java](DEMO/src/main/java/io/cloudwalk/pos/demo/DEMO.java) for
    samples of requests made through the `JSON` API_.

An ABECS PINPAD natively exchanges `byte[]` streams. However, the Pinpad
Service allows local requests made through a `JSON` API, for easier
data handling[^2]. The Pinpad Library is the one responsible for the conversion
between `JSON` and `byte[]`.  

[^2]: Usage of the `JSON` API is highly recommended over the `byte` API.

- _JSON_ API
  - `PinpadManager#interrupt();`
  - `PinpadManager#request(String, IServiceCallback);`
- _byte[]_ API
  - `PinpadManager#send(byte[], int, IServiceCallback);`
  - `PinpadManager#receive(byte[], long);`
