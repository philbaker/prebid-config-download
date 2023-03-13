# Prebid config download
Uses [playwright](https://playwright.dev/) to download a custom version of prebid
from https://docs.prebid.org/download

## Usage
`npm i pb-config-dl`

Create a config.edn file with the required version, adapters, modules and
whether the browser should be headless. If no config.edn exists a file with
the following config will be created

```clojure
{:prebid-version "7.38.0" 
 :prebid-adapters ["adWMG" "aja" "ablida"] 
 :file-prefix "prebid-" 
 :headless false}
```

run `pb-config-dl`

prebid.js, a screenshot of the Prebid config from the browser and a log file are 
saved to the /out directory
