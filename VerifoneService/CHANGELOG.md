# CHANGELOG

## Dependencies
- Android 5.1.1
- Baseband BP01.003(SC20AUSAR01A03_SX)
- Firmware V1.0.1.20200313
- Linux Kernel 3.10.49
- ROM V1.1.0.202003131201 BRA
- Security Driver VA.182.S.040.201911211114
- VFService 2.22.2
- VFSystemService 1.8.11
- BC 001.19

## Known issues
- SETIS components don't notify PIN capture start and/or finish events, which
  prevents PIN keyboard customization _per_ package (defaults to InfinitePay).
- `GPN` and `GOX` triggers the instantiation of a translucent activity, which
  is vulnerable to user interactions between its own creation and PIN keyboard
  exhibition.
- `MNU` and `GCD` will always disregard user input and return `ST_TIMEOUT`.
- EMV LED operation is disabled.
- Local responses will always disregard `PP_DSPxxx` and `PP_TLRMEM`.

## [1.0.0] - YYYY-MM-DD
- Initial release.
