# CHANGELOG

## Dependencies

### X990
- Android 5.1.1
- Baseband BP01.003(SC20AUSAR01A03_SX)
- Firmware V1.0.1.20210130
- Linux Kernel 3.10.49
- ROM V1.1.0.202101301407 BRA
- Security Driver VA.215.S.076.202202101445
- VFService 3.11.2.3
- VFSystemService 1.8.11
- BC 001.29

## Known issues
- PIN keyboard external layout customization is missing.
- Callbacks aren't interrupted by `ST_TIMEOUT`. Caller has to oversight time.
- `MNU` and `GCD` will always disregard user input.
- `PP_TLRMEM` and multimedia related TAGs are out of scope and its values
  should be ignored.
  
## [1.1.4] - 2022-03-23
- Loose restrictions to perform a PIN capture operation as much as possible,
  in order to avoid freezes.

## [1.1.3] - 2022-03-16
- Update default PIN keyboard layout background.

## [1.1.2] - 2022-03-15
- Update default PIN keyboard layout customization code.

## [1.1.1] - 2022-03-15
- Update default PIN keyboard layout.
  
## [1.1.0] - 2022-02-24
- Completely rewrite internal byte stream handling routines.

## [1.0.9] - 2022-02-23
- Update dependencies and compilation tools.

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

## [1.0.3.x] - 2022-03-23 - WARNING: v1.0.3.x mirrors v1.1.3+
Due to the lack of support for updates of several components on older X990
devices, these versions replicate all improvements of v1.1.3+, although missing
all of the fixes from its dependencies.  
In the event of a leveling opportunity, these versions will only accept to be
updated or replaced by its correspondents v1.1.x, a equivalent or newer.  

- 1.0.3.1 mirrors 1.1.3.
- 1.0.3.2 mirrors 1.1.4.

## [1.0.3] - 2021-10-11
- Add `DUKLINK.dat` creation support.

## [1.0.2] - 2021-10-08
- Update PIN keyboard layout.

## [1.0.1] - 2021-10-05 - WARNING: incompatible with previous release(s)
- Update public API for easier polymorphism simulation.

## [1.0.0] - 2021-10-04
- Initial release.
