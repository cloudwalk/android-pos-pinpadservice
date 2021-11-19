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
- PayLib 1.4.60
- BC 1.29

## Known issues
- PIN keyboard external customization is disabled.
- Callbacks are not interrupted by `ST_TIMEOUT`. Caller application has to
  oversight time passing.
- `MNU` and `GCD` will always disregard user input and return `ST_TIMEOUT`.
- `PP_TLRMEM` is untreated and its values should be ignored.
- `PP_DSPxxx` - as well as other multimedia related TAGs - are out of scope and
  its values should be ignored.

## [M.m.b] - YYYY-MM-DD
- Update dependencies and compilation tools.

## [1.0.4] - 2021-10-26
- Update vendor components.
- Update dependencies.
- Enable EMV LED operation.
  
## [1.0.3] - 2021-10-11
- Add `keymap.dat` creation support.
  
## [1.0.2] - 2021-10-08
- Update PIN keyboard layout.
- Update vendor dependencies.

## [1.0.1] - 2021-10-05 - WARNING: incompatible with previous release(s)
- Update public API for easier polymorphism simulation.

## [1.0.0] - 2021-09-30
- Initial release.
