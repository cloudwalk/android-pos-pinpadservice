# CHANGELOG

## Dependencies
- Android 5.1.1
- Baseband BP01.003(SC20AUSAR01A03_SX)
- Firmware V1.0.1.20200313
- Linux Kernel 3.10.49
- ROM V1.1.0.202003131201 BRA
- Security Driver VA.193.S.046.202006111742
- VFService 3.10.3
- VFSystemService 1.8.11
- BC 001.23

## Known issues
- SETIS components don't notify PIN capture start events, which prevents PIN
  keyboard customization _per_ package (defaults to InfinitePay).
- PIN keyboard external customization is disabled.
- Callbacks are not interrupted by `ST_TIMEOUT`. Caller application has to
  oversight time passing.
- `MNU` and `GCD` will always disregard user input and return `ST_TIMEOUT`.
- `PP_TLRMEM` is untreated and its values should be ignored.
- `PP_DSPxxx` - as well as other multimedia related TAGs - are out of scope and
  its values should be ignored.

## [1.0.4] - YYYY-MM-DD
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
