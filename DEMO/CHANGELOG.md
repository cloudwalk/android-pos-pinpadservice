# CHANGELOG

## Dependencies

### Sunmi
- Pinpad Service 1.0.4

### Verifone 
- Pinpad Service 1.0.3

## Known issues
- Loosing focus for any reason will forcefully restart all DEMO routines.
- No keyboard interaction is available at the moment: upon multiple choice
  menus, the first option will be selected automatically.

## [0.0.12] - 2022-07-03
- Update dependencies.
- Rollback changes to the history stack in order to fix GPN/GOX critical
  failures, introduced in release 0.0.6.
- Replace `Bundle` API by `JSON` API to simplify internal coding and retain
  request/response data order when parsing data streams.

## [0.0.11] - 2022-06-15
- Update application theme.
- Remove application from backup and restore infrastructure.

## [0.0.10] - 2022-06-08
- Update dependencies.
- Lock screen orientation (portrait).

## [0.0.9] - 2022-06-03
- Update dependencies.
- Disable additional local requests from previous version.
- Review CHANGELOG.md.

## [0.0.8.x] - WARNING: interns usage only
- Enable additional local requests for internal validation.

## [0.0.8] - 2022-04-20
- Ensure the `Back` button won't trigger an application reload cycle, simply
  moving the activity stack to the background.

## [0.0.7] - 2022-04-19
- Review application reload when coming from the history stack.

## [0.0.6] - 2022-04-19
- Ensure proper process kill only - and only if - it's explicitly sent to
  background by the user.

## [0.0.5] - 2022-03-30
- Update poorly implemented callbacks to avoid unexpected exceptions.

## [0.0.4] - 2022-02-09
- Adjust minor UX aspects.

## [0.0.3] - 2022-02-04
- Fully integrate Pinpad Service Server.

## [0.0.2] - 2022-02-04
- Draft server with monochromatic display.

## [0.0.1] - 2021-10-27
- Initial release.
