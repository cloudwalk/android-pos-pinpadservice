# CHANGELOG

## Dependencies

### P2-B
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
- SunmiPaySDK 3.3.148
- BC 1.30

## Known issues
- PIN keyboard layout external customization is disabled.
- Callbacks don't run on their own threads, therefore may fail to properly track
  processing timeout. Caller has to oversight time.
- `MNU` and `GCD` were not fully developed and may not work as specified.
- `PP_TLRMEM` and multimedia related TAGs are out of scope and its values
  should be ignored.

## [1.1.6] - YYYY-MM-DD
- Update dependencies.
- Remove application from backup and restore infrastructure.
- Replace `Bundle` API by `JSON` API to simplify internal coding and retain
  request/response data order when parsing data streams.
- Greatly increase data handling security.

## [1.1.5] - 2022-03-27
- Broaden PIN capture activity updates to whenever a callback event is
  triggered.

## [1.1.4] - 2022-03-23
- Loose restrictions to perform a PIN capture operation.

## [1.1.3] - 2022-03-18
- Update default PIN keyboard layout background.

## [1.1.2] - 2022-03-15
- Update default PIN keyboard layout customization code.
  
## [1.1.1] - 2022-03-15
- Update default PIN keyboard layout.

## [1.1.0] - 2022-02-24
- Completely rewrite internal byte stream handling routines.

## [1.0.5] - 2022-02-23
- Update dependencies and compilation tools.

## [1.0.4] - 2021-10-26
- Update vendor dependencies.
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
