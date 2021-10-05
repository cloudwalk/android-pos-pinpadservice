# CHANGELOG

## Dependencies
- Android 7.1.1
- OS 3.0.52
- Firmware 348
- FW 1.0.0
- SV 1.7.6
- Sunmi OS 1.4.0
- Sunmi Version Code 20
- Sunmi SDK 1.2.0
- Super Power 2.2.1
- System State Manager 2.8.1
- PayLib 1.4.59
- BC 1.27

## Known issues
- EMV LED operation is disabled.
- `MNU` and `GCD` will always disregard user input and return `ST_TIMEOUT`.
- `PP_TLRMEM` is untreated and its values should be ignored.
- `PP_DSPxxx` (as well as other multimedia related TAGs) are out of scope and
  its values should be ignored.

## [1.0.1] - YYYY-MM-DD
- Update main API for easier polymorphism simulation (incompatible w/ previous
  release).

## [1.0.0] - 2021-09-30
- Initial release.
