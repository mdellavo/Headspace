import sys
import json
from time import sleep
import requests

from pprint import pprint

ROCKRADIO = "rockradio"
DI = "di"
RADIOTUNES = "radiotunes"
JAZZRADIO = "jazzradio"

VARIANTS = [DI, JAZZRADIO, RADIOTUNES, ROCKRADIO]

URLS = {
    JAZZRADIO: "http://www.jazzradio.com",
    RADIOTUNES: "http://www.radiotunes.com",
    DI: "http://www.di.fm",
    ROCKRADIO: "http://www.rockradio.com"
}

HOSTS = {
    ROCKRADIO: "listen.rockradio.com",
    DI: "listen.di.fm",
    RADIOTUNES: "listen.radiotunes.com",
    JAZZRADIO: "listen.jazzradio.com"
}

STREAM_LISTS = {
    ROCKRADIO: "android",
    DI: "android_high",
    RADIOTUNES: "public3",
    JAZZRADIO: "appleapp"
}

NETWORKS = {
    ROCKRADIO: "RockRadio.com",
    DI: "Digitally Imported",
    RADIOTUNES: "RadioTunes.com",
    JAZZRADIO: "JazzRadio.com"
}


def get_streams(variant, key):

    response = None
    count = 0
    while not response or response.status_code == 429:
        count += 1
        if count > 10:
            raise RuntimeError("couldnt fetch {} / {}".format(variant, key))

        if count > 1:
            sleep(10)
        url = "http://{}/{}/{}".format(HOSTS[variant], STREAM_LISTS[variant], key)
        response = requests.get(url)

    return response.json()


def get_variant(stream_url):
    for variant, url in URLS.items():
        if stream_url.startswith(url):
            return variant


def build_station(channels):
    for channel in channels:
        variant = get_variant(channel["tunein_url"])
        streams = get_streams(variant, channel["key"])
        yield dict(network=NETWORKS[variant],
                   stationUrl=channel["tunein_url"],
                   name=channel["name"],
                   description=channel["description"],
                   streams=streams,
                   iconUrl="http:" + channel["asset_url"],
                   commercials=True)
        sleep(.5)



def main():

    channels = json.load(sys.stdin)
    stations = list(build_station(channels))
    print json.dumps(stations, sort_keys=True, indent=4)


if __name__ == "__main__":
    main()
