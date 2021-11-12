# CHANGELOG

## Dependencies

### Sunmi
- Pinpad Service 1.0.4

### Verifone 
- Pinpad Service 1.0.3

## Known issues
- Callbacks for service connection won't stack up.

## [1.0.7] - YYYY-MM-DD
- Greatly increases consistency when handling message exchanges from the
  `Bundle` interface.
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
