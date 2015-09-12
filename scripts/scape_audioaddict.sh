scrape () {
    BODY=`curl $1 |grep app\.start`
    BODY=${BODY##  di.app.start(}
    BODY=${BODY%%);}
    echo "${BODY}" | jq .channels > $2
}

#scrape "http://www.jazzradio.com/" jazzradio.raw.json
#scrape "http://www.rockradio.com/" rockradio.raw.json
#scrape "http://www.di.fm/" di.raw.json
#scrape "http://www.radiotunes.com/" radiotunes.raw.json

jq -s add jazzradio.raw.json rockradio.raw.json di.raw.json radiotunes.raw.json | python scrape_audioaddict.py
