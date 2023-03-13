# Prebid config download

Uses [playwright](https://playwright.dev/) to download a custom version of prebid
from https://docs.prebid.org/download

## Usage
`npm i prebid-config-download`

Create a config.edn file with the required version, adapters and modules (see
config.edn.example)

run `prebid-config-download`

prebid.js, a full page screenshot of the Prebid config and log with config information
are saved to the /out directory
