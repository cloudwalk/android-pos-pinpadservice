# Pinpad Service

Originally built as a (P)roof (O)f (C)oncept, the Pinpad Service serves as a
virtual ABECS PINPAD for POS solutions.  
From the entire ABECS specification, multimedia, proprietary and deprecated
instructions are left out, reducing scope to 18 instructions:  

- OPN, GIN and CLX
- CEX, CHP, EBX, GCD, GPN, GTK, MNU and RMC
- TLI, TLR and TLE
- GCX, GED, GOX and FCX

Be sure to check [ABECS](https://www.abecs.org.br/) website and
[spec.](https://www.abecs.org.br/certificacao-funcional-dos-pinpads) to have a
deeper understanding on how to make requests and handle its responses.  

Made available in its own installable package, the Pinpad Service can be
shared by several applications and updated independently, reducing costs of OTA
data usage and code maintenance.  
The Library that pairs with it helps to hide the complexity of binding the
service, as well as handling AIDL restrictions. It serves as handler and it was
designed to avoid otherwise direct connections.  

Be sure to check the [DEMO application](#demo-application) for an example on
how to consume the Pinpad Service using the associated Pinpad Library.

## Local dependencies

Local dependencies are those which are _private_, but within the scope of the
Pinpad Service development team. They need to be made available locally before
the Pinpad Service can be built:  

1. Clone the repositories
   [android-misc-loglibrary](https://github.com/mauriciospinardi-cloudwalk/android-misc-loglibrary)
   and [android-misc-utilitieslibrary](https://github.com/mauriciospinardi-cloudwalk/android-misc-utilitieslibrary)
2. Rebuild their `release` variants based on tags of your choice.
3. Run tasks: `gradle publishToMavenLocal`

## Copyrighted dependencies

Copyrighted dependencies are those provided by third parties. They are under
NDA and can't be made public.  
For each platform integrated to the Pinpad Service, reach out the respective
platform representatives.  

## DEMO application

The Pinpad Service includes a DEMO module, covering the very basics:  

- [SplashActivity.java](DEMO/src/main/java/io/cloudwalk/pos/demo/presentation/SplashActivity.java)
  shows how to connect the service using the Pinpad Library.
- [MainActivity.java](DEMO/src/main/java/io/cloudwalk/pos/demo/presentation/MainActivity.java)
  shows how to perform local requests.
  - _Check [DEMO.java](DEMO/src/main/java/io/cloudwalk/pos/demo/DEMO.java) for
    samples of requests made through the `Bundle` interface_.

An ABECS PINPAD natively exchanges `byte[]` streams. However, the Pinpad
Service allows local requests made through a `Bundle` interface, for easier
handling. The Pinpad Library is the one responsible for the conversion between
`Bundle` and `byte[]`. In any case, if one decides to use the native `byte[]`
interface, it can do so:

- _Bundle_ API
  - `PinpadManager#abort();`
  - `PinpadManager#request(Bundle, IServiceCallback);`
- _byte[]_ API
  - `PinpadManager#send(byte[], int, IServiceCallback);`
  - `PinpadManager#receive(byte[], long);`

<!-- TODO: summary of the `Bundle` API characteristics -->
