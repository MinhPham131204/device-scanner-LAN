# Changelog

## [1.0.0] - 2026-07-07
- Initial release of LAN Scanner.
- Using Ping to discover all active devices in LAN.
- Port scanning with IANA mapping.
- Retrieving network info (Location, Subnet, Gateway, Netmask, DNS).

## [1.1.0] - 2026-07-24
### Added
- Combine passive mDNS sniffing and ICMP Ping to accurately resolve hostnames of active devices.
- Add fallback mechanism to identify device vendors (e.g., Apple, Chromecast) via mDNS service types if the exact hostname is unavailable.