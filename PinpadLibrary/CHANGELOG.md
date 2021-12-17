# CHANGELOG

## Dependencies

### Sunmi
- Pinpad Service 1.0.4

### Verifone 
- Pinpad Service 1.0.3

## Known issues
- Callbacks for service connection won't stack up.

## [1.0.14] - 2021-12-17
- Update data conversion from `Bundle` interface for `OPN`, `CHP` and `GPN`.
- Update build tools.

## [1.0.13] - 2021-11-29
- Update internal dependencies.

## [1.0.12] - 2021-11-29
- Update internal dependencies.

## [1.0.11] - 2021-11-26
- Update logs.

## [1.0.10] - 2021-11-24
- Ensure single reading channel when using the `Bundle` API.

## [1.0.9] - 2021-11-24
- Ceases automatic request retries.
- Ensure consistency when mixing APIs in a threaded environment.
- Update logs.

## [1.0.8] - 2021-11-16
- Update logs.

## [1.0.7] - 2021-11-12
- Greatly increases consistency when handling message exchanges from the
  `Bundle` API.
- Forcefully send a single `<<CAN>>` byte upon abort requests.

## [1.0.6] - 2021-11-12
- Update logs.

## [1.0.5] - 2021-11-02
- Explicitly clear the communication channel before each request.
  
## [1.0.4] - 2021-10-21
- Add CHANGELOG.md.
- Update public API for compliance with community standards.

## [1.0.3] - 2021-10-11
- Add vendor specific key mapping creation support.

## [1.0.2] - 2021-10-05
- Update service communication using its newest public API.

## [1.0.1] - 2021-10-04
- Update dependencies.
- Forcefully send a triple `<<CAN>>` byte upon abort requests.

## [1.0.0] - 2021-09-30
- Initial release.
