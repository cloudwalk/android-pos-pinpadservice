# CHANGELOG

## Dependencies
- Android 5.1 (API 22)

## Known issues
- Callbacks don't run on their own threads, therefore may fail to properly track
  processing timeout.
- `MNU` and `GCD` were not fully developed and may not work as specified.

## [0.0.5] - YYYY-MM-DD
- Update dependencies.
- Replace `Bundle` API by `JSON` API to simplify internal coding and retain
  request/response data order when parsing data streams.

## [0.0.4] - 2022-06-15
- Update dependencies.
- Remove application from backup and restore infrastructure.

## [0.0.3] - 2022-06-08
- Update dependencies.
- Intercept `GIX` response to inject `VIRTUAL//` into `PP_MODEL`.
- Lock screen orientation (portrait).

## [0.0.2] - 2022-02-24
- Rewrite internal byte stream handling routines.

## [0.0.1] - 2022-02-09
- Initial release.
