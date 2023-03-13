# Prebid config download
Uses [playwright](https://playwright.dev/) to download a custom version of prebid
from https://docs.prebid.org/download

## Usage
`npm i pb-config-dl`

Create a config.edn file with the required version, adapters and modules (see
config.edn.example)

run `pb-config-dl`

prebid.js, a screenshot of the Prebid config from the browser and a log file are 
saved to the /out directory
