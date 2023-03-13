# Prebid config download
Uses [playwright](https://playwright.dev/) to download a custom version of prebid
from https://docs.prebid.org/download

## Usage
Create a config.edn file with your version, adapters, file prefix and whether
the browser should be headless

If no config.edn file is found the following example config will be created

```clojure
{:prebid-version "7.38.0" 
 :prebid-adapters ["adWMG" "aja" "ablida"] 
 :file-prefix "prebid-" 
 :headless false}
```

To execute the script run `npx pb-config-dl` 

prebid.js, a screenshot of the Prebid config from the browser and a log file are 
saved to the /out directory
