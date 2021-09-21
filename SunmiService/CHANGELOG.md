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
- `GPN` and `GOX` will trigger the instantiation of a translucent activity,
  which is vulnerable to interactions between its own creation and PIN keyboard
  exhibition.
- `MNU` and `GCD` will always disregard user input and return `ST_TIMEOUT`.
- EMV LED operation is disabled.
- Local responses will always disregard `PP_DSPxxx` and `PP_TLRMEM`.

## [1.0.0] - YYYY-MM-DD
- Initial release.
