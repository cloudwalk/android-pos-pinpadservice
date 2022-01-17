# CHANGELOG

## Dependencies
- Android 5.1.1
- Baseband BP01.003(SC20AUSAR01A03_SX)
- Firmware V1.0.1.20210909
- Linux Kernel 3.10.49
- ROM V1.1.0.202109090928 BRA
- Security Driver VA.213.S.068.202108241801
- VFService 3.11.0
- VFSystemService 1.8.11
- BC 001.27

## Known issues
- PIN keyboard external customization is disabled.
- Callbacks are not interrupted by `ST_TIMEOUT`. Caller application has to
  oversight time passing.
- `MNU` and `GCD` will always disregard user input and return `ST_TIMEOUT`.
- `PP_TLRMEM` and `PP_DSPxxx` - as well as other multimedia related TAGs - are
  out of scope and its values should be ignored.

## [1.0.8] - 2022-01-17
- Update vendor components.
  
## [1.0.7] - 2021-12-07
- Update dependencies and compilation tools.
- Ensure the proper components variants will be used in each build type.
- Update vendor components.

## [1.0.6] - 2021-11-10
- Update vendor components.

## [1.0.5] - 2021-11-03
- Update vendor components.

## [1.0.4] - 2021-10-27
- Update vendor components.
- Update dependencies.
- Enable EMV LED operation.

## [1.0.3] - 2021-10-11
- Add `DUKLINK.dat` creation support.

## [1.0.2] - 2021-10-08
- Update PIN keyboard layout.

## [1.0.1] - 2021-10-05 - WARNING: incompatible with previous release(s)
- Update public API for easier polymorphism simulation.

## [1.0.0] - 2021-10-04
- Initial release.
