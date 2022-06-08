# CHANGELOG

## Dependencies

### Sunmi
- Pinpad Service 1.0.4

### Verifone 
- Pinpad Service 1.0.3

## [1.1.0] - 2022-06-08
- Update dependencies.
- Unify handling of TLV data for multiple commands.
- Add response byte stream builder:
  - `PinpadUtility#buildResponseDataPacket(Bundle)`

## [1.0.18] - 2022-06-03
- Update dependencies.
- Review CHANGELOG.md.
- Optimize ASCII data conversion from byte arrays.

## [1.0.17] - 2022-05-21
- Update dependencies.

## [1.0.16] - 2022-02-24
- Improve `Bundle` API consistency when facing improper or unanticipated
  interruptions.
- Prefer `ByteArrayOutputStream` to trim byte streams whenever possible.
- Review documentation. 

## [1.0.15] - 2022-02-23
- Workaround eventual `<<ACK>>` trash in vendor streams.

## [1.0.14] - 2021-12-17
- Update `OPN`, `CHP` and `GPN` response packet parsing.
- Update build tools.

## [1.0.13] - 2021-11-29
- Update dependencies.

## [1.0.12] - 2021-11-29
- Update dependencies.

## [1.0.11] - 2021-11-26
- Update logs.

## [1.0.10] - 2021-11-24
- Ensure single reading channel when using the `Bundle` API.

## [1.0.9] - 2021-11-24
- Cease automatic request retries.
- Ensure consistency when mixing APIs in a threaded environment.
- Update logs.

## [1.0.8] - 2021-11-16
- Update logs.

## [1.0.7] - 2021-11-12
- Greatly increase consistency of message exchange when using the `Bundle` API.
- Forcefully send a single `<<CAN>>` byte upon abort requests.

## [1.0.6] - 2021-11-12
- Update logs.

## [1.0.5] - 2021-11-02
- Explicitly clear the communication channel before each request.
  
## [1.0.4] - 2021-10-21
- Add CHANGELOG.md.
- Update public API for compliance with community standards.

## [1.0.3] - 2021-10-11
- Add key mapping creation support.

## [1.0.2] - 2021-10-05
- Update service message exchange.

## [1.0.1] - 2021-10-04
- Update dependencies.
- Forcefully send a triple `<<CAN>>` byte upon abort requests.

## [1.0.0] - 2021-09-30
- Initial release.
